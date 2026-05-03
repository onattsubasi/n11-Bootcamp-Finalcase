package com.onatsubasi.finalcase.payment.infrastructure.provider.iyzico;

import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreateCancelRequest;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.CreateRefundRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import com.onatsubasi.finalcase.payment.application.dto.provider.*;
import com.onatsubasi.finalcase.payment.application.port.PaymentProviderPort;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentAttemptStatus;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import com.onatsubasi.finalcase.payment.infrastructure.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class IyzicoPaymentProviderAdapter implements PaymentProviderPort {

    private static final String SUCCESS = "success";

    private final Options iyzicoOptions;
    private final IyzicoPaymentMapper iyzicoPaymentMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentProviderCode providerCode() {
        return PaymentProviderCode.IYZICO;
    }

    @Override
    public ProviderCapability capability() {
        return ProviderCapability.builder()
                .provider(PaymentProviderCode.IYZICO)
                .supportedMethods(Set.of(
                        PaymentMethod.CHECKOUT_FORM,
                        PaymentMethod.PAY_WITH_IYZICO
                ))
                .supportsRefund(true)
                .supportsCancel(true)
                .supportsInstallments(true)
                .requiresRedirect(true)
                .requiresCallback(true)
                .build();
    }

    @Override
    public ProviderPaymentInitializeResult initializePayment(
            ProviderPaymentInitializeCommand command
    ) {
        try {
            log.info(
                    "event=payment.iyzico_initialize_started paymentId={} attemptId={} orderId={} amount={} currency={}",
                    command.paymentId(),
                    command.paymentAttemptId(),
                    command.orderId(),
                    command.amount(),
                    command.currency()
            );

            CreateCheckoutFormInitializeRequest request =
                    iyzicoPaymentMapper.toCheckoutFormInitializeRequest(command);

            CheckoutFormInitialize response =
                    CheckoutFormInitialize.create(request, iyzicoOptions);

            Map<String, Object> snapshot = paymentMapper.toMap(response);

            boolean success = isSuccess(response.getStatus());

            if (!success) {
                return ProviderPaymentInitializeResult.builder()
                        .success(false)
                        .attemptStatus(PaymentAttemptStatus.FAILED)
                        .paymentStatus(PaymentStatus.FAILED)
                        .providerStatus(response.getStatus())
                        .failureReason(response.getErrorMessage())
                        .providerResponseSnapshot(snapshot)
                        .build();
            }

            return ProviderPaymentInitializeResult.builder()
                    .success(true)
                    .attemptStatus(PaymentAttemptStatus.WAITING_PROVIDER_ACTION)
                    .paymentStatus(PaymentStatus.WAITING_PROVIDER_ACTION)
                    .providerToken(response.getToken())
                    .providerConversationId(response.getConversationId())
                    .providerStatus(response.getStatus())
                    .paymentPageUrl(response.getPaymentPageUrl())
                    .checkoutFormContent(response.getCheckoutFormContent())
                    .providerResponseSnapshot(snapshot)
                    .build();
        } catch (Exception ex) {
            log.error(
                    "event=payment.iyzico_initialize_failed paymentId={} attemptId={} orderId={}",
                    command.paymentId(),
                    command.paymentAttemptId(),
                    command.orderId(),
                    ex
            );

            return ProviderPaymentInitializeResult.builder()
                    .success(false)
                    .attemptStatus(PaymentAttemptStatus.FAILED)
                    .paymentStatus(PaymentStatus.FAILED)
                    .failureReason("Iyzico initialize failed")
                    .providerResponseSnapshot(Map.of())
                    .build();
        }
    }

    @Override
    public ProviderPaymentRetrieveResult retrievePayment(
            ProviderPaymentRetrieveCommand command
    ) {
        try {
            log.info(
                    "event=payment.iyzico_retrieve_started paymentId={} attemptId={}",
                    command.paymentId(),
                    command.paymentAttemptId()
            );

            RetrieveCheckoutFormRequest request =
                    iyzicoPaymentMapper.toRetrieveCheckoutFormRequest(command);

            CheckoutForm response = CheckoutForm.retrieve(request, iyzicoOptions);

            Map<String, Object> snapshot = paymentMapper.toMap(response);

            boolean success = isSuccess(response.getStatus())
                    && "SUCCESS".equalsIgnoreCase(response.getPaymentStatus());

            if (!success) {
                return ProviderPaymentRetrieveResult.builder()
                        .success(false)
                        .paymentStatus(PaymentStatus.FAILED)
                        .attemptStatus(PaymentAttemptStatus.FAILED)
                        .providerPaymentId(response.getPaymentId())
                        .providerConversationId(response.getConversationId())
                        .providerStatus(response.getPaymentStatus())
                        .failureReason(response.getErrorMessage())
                        .paidAmount(response.getPaidPrice())
                        .currency(response.getCurrency())
                        .providerResponseSnapshot(snapshot)
                        .build();
            }

            String transactionId = extractFirstTransactionId(response);

            return ProviderPaymentRetrieveResult.builder()
                    .success(true)
                    .paymentStatus(PaymentStatus.SUCCEEDED)
                    .attemptStatus(PaymentAttemptStatus.SUCCEEDED)
                    .providerPaymentId(response.getPaymentId())
                    .providerTransactionId(transactionId)
                    .providerConversationId(response.getConversationId())
                    .providerStatus(response.getPaymentStatus())
                    .paidAmount(response.getPaidPrice())
                    .currency(response.getCurrency())
                    .providerResponseSnapshot(snapshot)
                    .build();
        } catch (Exception ex) {
            log.error(
                    "event=payment.iyzico_retrieve_failed paymentId={} attemptId={}",
                    command.paymentId(),
                    command.paymentAttemptId(),
                    ex
            );

            return ProviderPaymentRetrieveResult.builder()
                    .success(false)
                    .paymentStatus(PaymentStatus.FAILED)
                    .attemptStatus(PaymentAttemptStatus.FAILED)
                    .failureReason("Iyzico retrieve failed")
                    .providerResponseSnapshot(Map.of())
                    .build();
        }
    }

    @Override
    public ProviderRefundResult refundPayment(ProviderRefundCommand command) {
        try {
            log.info(
                    "event=payment.iyzico_refund_started paymentId={} refundId={} amount={} currency={}",
                    command.paymentId(),
                    command.refundId(),
                    command.amount(),
                    command.currency()
            );

            CreateRefundRequest request = iyzicoPaymentMapper.toRefundRequest(command);

            Refund response = Refund.create(request, iyzicoOptions);

            boolean success = isSuccess(response.getStatus());

            return ProviderRefundResult.builder()
                    .success(success)
                    .providerRefundId(response.getPaymentTransactionId())
                    .providerStatus(response.getStatus())
                    .failureReason(success ? null : response.getErrorMessage())
                    .providerResponseSnapshot(paymentMapper.toMap(response))
                    .build();
        } catch (Exception ex) {
            log.error(
                    "event=payment.iyzico_refund_failed paymentId={} refundId={}",
                    command.paymentId(),
                    command.refundId(),
                    ex
            );

            return ProviderRefundResult.builder()
                    .success(false)
                    .failureReason("Iyzico refund failed")
                    .providerResponseSnapshot(Map.of())
                    .build();
        }
    }

    @Override
    public ProviderCancelResult cancelPayment(ProviderCancelCommand command) {
        try {
            log.info(
                    "event=payment.iyzico_cancel_started paymentId={} cancellationId={}",
                    command.paymentId(),
                    command.cancellationId()
            );

            CreateCancelRequest request = iyzicoPaymentMapper.toCancelRequest(command);

            Cancel response = Cancel.create(request, iyzicoOptions);

            boolean success = isSuccess(response.getStatus());

            return ProviderCancelResult.builder()
                    .success(success)
                    .providerCancelId(response.getPaymentId())
                    .providerStatus(response.getStatus())
                    .failureReason(success ? null : response.getErrorMessage())
                    .providerResponseSnapshot(paymentMapper.toMap(response))
                    .build();
        } catch (Exception ex) {
            log.error(
                    "event=payment.iyzico_cancel_failed paymentId={} cancellationId={}",
                    command.paymentId(),
                    command.cancellationId(),
                    ex
            );

            return ProviderCancelResult.builder()
                    .success(false)
                    .failureReason("Iyzico cancel failed")
                    .providerResponseSnapshot(Map.of())
                    .build();
        }
    }

    private boolean isSuccess(String status) {
        return status != null && SUCCESS.equalsIgnoreCase(status);
    }

    private String extractFirstTransactionId(CheckoutForm response) {
        if (response.getPaymentItems() == null || response.getPaymentItems().isEmpty()) {
            return null;
        }

        return response.getPaymentItems()
                .get(0)
                .getPaymentTransactionId();
    }
}