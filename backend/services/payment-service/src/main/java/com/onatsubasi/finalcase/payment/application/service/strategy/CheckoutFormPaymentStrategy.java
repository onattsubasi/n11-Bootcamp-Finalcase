package com.onatsubasi.finalcase.payment.application.service.strategy;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.application.dto.request.InitializePaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentInitializeResponse;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderPaymentInitializeResult;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import com.onatsubasi.finalcase.payment.infrastructure.mapper.PaymentMapper;
import com.onatsubasi.finalcase.payment.application.service.PaymentProviderFactory;
import com.onatsubasi.finalcase.payment.application.port.PaymentProviderPort;
import com.onatsubasi.finalcase.payment.domain.model.Payment;
import com.onatsubasi.finalcase.payment.domain.model.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutFormPaymentStrategy implements PaymentMethodStrategy {

        private final PaymentProviderFactory paymentProviderFactory;
        private final PaymentMapper paymentMapper;

        @Override
        public PaymentMethod method() {
                return PaymentMethod.CHECKOUT_FORM;
        }

        @Override
        public PaymentInitializeResponse initialize(
                        Payment payment,
                        PaymentAttempt attempt,
                        InitializePaymentRequest request) {
                PaymentProviderPort provider = paymentProviderFactory.getProvider(
                                payment.getProvider());

                if (!provider.supports(PaymentMethod.CHECKOUT_FORM)) {
                        throw new BaseException(PaymentErrorCode.PAYMENT_PROVIDER_METHOD_NOT_SUPPORTED);
                }

                log.info(
                                "event=payment.provider_initialize_started paymentId={} attemptId={} provider={} method={}",
                                payment.getId(),
                                attempt.getId(),
                                payment.getProvider(),
                                payment.getMethod());

                ProviderPaymentInitializeResult result = provider.initializePayment(
                                paymentMapper.toProviderInitializeCommand(
                                                payment,
                                                attempt,
                                                request));

                if (!result.success()) {
                        attempt.markFailed(
                                        result.providerPaymentId(),
                                        result.providerTransactionId(),
                                        result.providerConversationId(),
                                        result.providerStatus(),
                                        result.failureReason(),
                                        result.providerResponseSnapshot());

                        log.warn(
                                        "event=payment.provider_initialize_failed paymentId={} attemptId={} provider={} reason={}",
                                        payment.getId(),
                                        attempt.getId(),
                                        payment.getProvider(),
                                        result.failureReason());

                        throw new BaseException(
                                        PaymentErrorCode.PAYMENT_INITIALIZE_FAILED,
                                        result.failureReason());
                }

                attempt.markInitialized(
                                result.providerToken(),
                                result.providerConversationId(),
                                result.paymentPageUrl(),
                                result.checkoutFormContent(),
                                result.providerResponseSnapshot());

                attempt.markWaitingProviderAction();
                payment.markWaitingProviderAction();

                log.info(
                                "event=payment.initialized paymentId={} attemptId={} provider={} method={}",
                                payment.getId(),
                                attempt.getId(),
                                payment.getProvider(),
                                payment.getMethod());

                return paymentMapper.toInitializeResponse(payment, attempt);
        }
}