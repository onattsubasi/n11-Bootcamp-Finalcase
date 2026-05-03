package com.onatsubasi.finalcase.payment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderPaymentRetrieveResult;
import com.onatsubasi.finalcase.payment.application.dto.request.IyzicoCheckoutFormCallbackRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentDetailResponse;
import com.onatsubasi.finalcase.payment.application.port.PaymentEventPublisher;
import com.onatsubasi.finalcase.payment.application.port.PaymentProviderPort;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import com.onatsubasi.finalcase.payment.domain.model.Payment;
import com.onatsubasi.finalcase.payment.domain.model.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.model.PaymentCallback;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentAttemptRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentCallbackRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRepository;
import com.onatsubasi.finalcase.payment.infrastructure.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackService {

    private final PaymentCallbackRepository callbackRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProviderFactory providerFactory;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentDetailResponse handleIyzicoCheckoutFormCallback(
            IyzicoCheckoutFormCallbackRequest request) {
        String token = normalizeRequiredToken(request.token());

        log.info(
                "event=payment.callback_received provider={} tokenPresent=true",
                PaymentProviderCode.IYZICO);

        PaymentCallback callback = callbackRepository
                .findByProviderAndEventKeyForUpdate(PaymentProviderCode.IYZICO, token)
                .orElseGet(() -> callbackRepository.save(
                        new PaymentCallback(
                                PaymentProviderCode.IYZICO,
                                token,
                                token,
                                callbackSnapshot(request))));

        if (callback.isProcessed()) {
            PaymentAttempt existingAttempt = paymentAttemptRepository
                    .findByProviderAndProviderToken(PaymentProviderCode.IYZICO, token)
                    .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));

            log.info(
                    "event=payment.callback_replayed provider={} paymentId={} attemptId={}",
                    PaymentProviderCode.IYZICO,
                    existingAttempt.getPayment().getId(),
                    existingAttempt.getId());

            return paymentMapper.toDetailResponse(existingAttempt.getPayment());
        }

        try {
            PaymentAttempt attempt = paymentAttemptRepository
                    .findByProviderAndProviderToken(PaymentProviderCode.IYZICO, token)
                    .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));

            Payment payment = paymentRepository
                    .findByIdForUpdate(attempt.getPayment().getId())
                    .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));

            attempt.markProviderProcessing();

            PaymentProviderPort provider = providerFactory.getProvider(PaymentProviderCode.IYZICO);
            ProviderPaymentRetrieveResult result = provider.retrievePayment(
                    paymentMapper.toProviderRetrieveCommand(attempt));

            if (result.success()) {
                validateProviderAmount(payment, result);

                attempt.markSucceeded(
                        result.providerPaymentId(),
                        result.providerTransactionId(),
                        result.providerConversationId(),
                        result.providerStatus(),
                        result.providerResponseSnapshot());

                payment.markSucceeded(
                        result.providerPaymentId(),
                        result.providerTransactionId(),
                        result.providerConversationId(),
                        result.providerStatus());

                Payment saved = paymentRepository.save(payment);
                callback.markProcessed();
                callbackRepository.save(callback);

                eventPublisher.publishPaymentSucceeded(saved);

                log.info(
                        "event=payment.callback_succeeded paymentId={} attemptId={} orderId={} providerPaymentId={}",
                        saved.getId(),
                        attempt.getId(),
                        saved.getOrderId(),
                        saved.getProviderPaymentId());

                return paymentMapper.toDetailResponse(saved);
            }

            attempt.markFailed(
                    result.providerPaymentId(),
                    result.providerTransactionId(),
                    result.providerConversationId(),
                    result.providerStatus(),
                    result.failureReason(),
                    result.providerResponseSnapshot());

            payment.markFailed(
                    result.providerPaymentId(),
                    result.providerTransactionId(),
                    result.providerConversationId(),
                    result.providerStatus(),
                    result.failureReason());

            Payment saved = paymentRepository.save(payment);
            callback.markProcessed();
            callbackRepository.save(callback);

            eventPublisher.publishPaymentFailed(saved);

            log.warn(
                    "event=payment.callback_failed paymentId={} attemptId={} orderId={} reason={}",
                    saved.getId(),
                    attempt.getId(),
                    saved.getOrderId(),
                    result.failureReason());

            return paymentMapper.toDetailResponse(saved);
        } catch (Exception ex) {
            callback.markProcessingError(ex.getMessage());
            callbackRepository.save(callback);

            log.error(
                    "event=payment.callback_processing_failed provider={} tokenPresent=true",
                    PaymentProviderCode.IYZICO,
                    ex);

            throw ex;
        }
    }

    private void validateProviderAmount(
            Payment payment,
            ProviderPaymentRetrieveResult result) {
        if (result.paidAmount() == null) {
            throw new BaseException(
                    PaymentErrorCode.PAYMENT_AMOUNT_INVALID,
                    "Provider paid amount is missing");
        }

        if (payment.getPaidAmount().compareTo(result.paidAmount()) != 0) {
            throw new BaseException(
                    PaymentErrorCode.PAYMENT_AMOUNT_INVALID,
                    "Provider paid amount does not match expected amount");
        }

        if (result.currency() != null
                && !payment.getCurrency().equalsIgnoreCase(result.currency())) {
            throw new BaseException(
                    PaymentErrorCode.PAYMENT_CURRENCY_INVALID,
                    "Provider currency does not match expected currency");
        }
    }

    private String normalizeRequiredToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BaseException(PaymentErrorCode.PAYMENT_CALLBACK_INVALID, "Iyzico callback token is required");
        }

        return token.trim();
    }

    private Map<String, Object> callbackSnapshot(IyzicoCheckoutFormCallbackRequest request) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("tokenPresent", request.token() != null && !request.token().isBlank());

        if (request.status() != null) {
            snapshot.put("status", request.status());
        }

        if (request.conversationId() != null) {
            snapshot.put("conversationId", request.conversationId());
        }

        return snapshot;
    }
}
