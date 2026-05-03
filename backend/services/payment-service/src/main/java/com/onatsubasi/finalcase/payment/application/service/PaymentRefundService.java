package com.onatsubasi.finalcase.payment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderCancelResult;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderRefundResult;
import com.onatsubasi.finalcase.payment.application.dto.request.CancelPaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.request.RefundPaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentCancellationResponse;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentRefundResponse;
import com.onatsubasi.finalcase.payment.application.port.PaymentEventPublisher;
import com.onatsubasi.finalcase.payment.application.port.PaymentProviderPort;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import com.onatsubasi.finalcase.payment.domain.model.Payment;
import com.onatsubasi.finalcase.payment.domain.model.PaymentCancellation;
import com.onatsubasi.finalcase.payment.domain.model.PaymentRefund;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentCancellationRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRefundRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRepository;
import com.onatsubasi.finalcase.payment.infrastructure.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository refundRepository;
    private final PaymentCancellationRepository cancellationRepository;
    private final PaymentProviderFactory providerFactory;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;
    private final PaymentRequestHashService requestHashService;

    @Transactional
    public PaymentRefundResponse refundPayment(
            UUID paymentId,
            String idempotencyKey,
            RefundPaymentRequest request) {
        requireIdempotencyKey(idempotencyKey);
        String normalizedKey = idempotencyKey.trim();
        String requestHash = requestHashService.hash(new RefundIdempotencyPayload(paymentId, request));

        PaymentRefund existingRefund = refundRepository
                .findByIdempotencyKeyForUpdate(normalizedKey)
                .orElse(null);

        if (existingRefund != null) {
            existingRefund.validateSameRequest(requestHash);
            return paymentMapper.toRefundResponse(existingRefund);
        }

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        log.info(
                "event=payment.refund_requested paymentId={} orderId={} amount={} currency={}",
                payment.getId(),
                payment.getOrderId(),
                request.amount(),
                request.currency());

        PaymentRefund refund = refundRepository.save(new PaymentRefund(
                payment,
                normalizedKey,
                requestHash,
                request.amount(),
                request.currency()));

        PaymentProviderPort provider = providerFactory.getProvider(payment.getProvider());
        ProviderRefundResult result = provider.refundPayment(
                paymentMapper.toProviderRefundCommand(payment, refund, request));

        if (result.success()) {
            refund.markSucceeded(result.providerRefundId(), result.providerStatus());
            payment.applyRefund(request.amount());

            Payment savedPayment = paymentRepository.save(payment);
            PaymentRefund savedRefund = refundRepository.save(refund);

            eventPublisher.publishPaymentRefunded(savedPayment, savedRefund);

            log.info(
                    "event=payment.refund_succeeded paymentId={} refundId={} status={}",
                    savedPayment.getId(),
                    savedRefund.getId(),
                    savedPayment.getStatus());

            return paymentMapper.toRefundResponse(savedRefund);
        }

        refund.markFailed(result.providerStatus(), result.failureReason());
        PaymentRefund savedRefund = refundRepository.save(refund);

        log.warn(
                "event=payment.refund_failed paymentId={} refundId={} reason={}",
                payment.getId(),
                savedRefund.getId(),
                result.failureReason());

        return paymentMapper.toRefundResponse(savedRefund);
    }

    @Transactional
    public PaymentCancellationResponse cancelPayment(
            UUID paymentId,
            String idempotencyKey,
            CancelPaymentRequest request) {
        requireIdempotencyKey(idempotencyKey);
        String normalizedKey = idempotencyKey.trim();
        String requestHash = requestHashService.hash(new CancelIdempotencyPayload(paymentId, request));

        PaymentCancellation existingCancellation = cancellationRepository
                .findByIdempotencyKeyForUpdate(normalizedKey)
                .orElse(null);

        if (existingCancellation != null) {
            existingCancellation.validateSameRequest(requestHash);
            return paymentMapper.toCancellationResponse(existingCancellation);
        }

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        log.info(
                "event=payment.cancel_requested paymentId={} orderId={} status={}",
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus());

        PaymentCancellation cancellation = cancellationRepository.save(new PaymentCancellation(
                payment,
                normalizedKey,
                requestHash));

        PaymentProviderPort provider = providerFactory.getProvider(payment.getProvider());
        ProviderCancelResult result = provider.cancelPayment(
                paymentMapper.toProviderCancelCommand(payment, cancellation, request));

        if (result.success()) {
            cancellation.markSucceeded(result.providerCancelId(), result.providerStatus());
            payment.markCancelled(result.providerStatus(), payment.getProviderTransactionId());

            Payment savedPayment = paymentRepository.save(payment);
            PaymentCancellation savedCancellation = cancellationRepository.save(cancellation);

            eventPublisher.publishPaymentCancelled(savedPayment, savedCancellation);

            log.info(
                    "event=payment.cancel_succeeded paymentId={} cancellationId={}",
                    savedPayment.getId(),
                    savedCancellation.getId());

            return paymentMapper.toCancellationResponse(savedCancellation);
        }

        cancellation.markFailed(result.providerStatus(), result.failureReason());
        PaymentCancellation savedCancellation = cancellationRepository.save(cancellation);

        log.warn(
                "event=payment.cancel_failed paymentId={} cancellationId={} reason={}",
                payment.getId(),
                savedCancellation.getId(),
                result.failureReason());

        return paymentMapper.toCancellationResponse(savedCancellation);
    }

    private void requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BaseException(PaymentErrorCode.PAYMENT_IDEMPOTENCY_KEY_REQUIRED);
        }
    }

    private record RefundIdempotencyPayload(UUID paymentId, RefundPaymentRequest request) {
    }

    private record CancelIdempotencyPayload(UUID paymentId, CancelPaymentRequest request) {
    }
}
