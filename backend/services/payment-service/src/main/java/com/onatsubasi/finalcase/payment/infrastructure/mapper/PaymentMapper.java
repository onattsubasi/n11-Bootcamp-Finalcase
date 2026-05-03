package com.onatsubasi.finalcase.payment.infrastructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.payment.application.dto.event.PaymentCancelledEvent;
import com.onatsubasi.finalcase.payment.application.dto.event.PaymentRefundedEvent;
import com.onatsubasi.finalcase.payment.application.dto.event.PaymentResultEvent;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderCancelCommand;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderPaymentInitializeCommand;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderPaymentRetrieveCommand;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderRefundCommand;
import com.onatsubasi.finalcase.payment.application.dto.request.CancelPaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.request.InitializePaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.request.RefundPaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.*;
import com.onatsubasi.finalcase.payment.infrastructure.config.PaymentServiceProperties;
import com.onatsubasi.finalcase.payment.domain.model.Payment;
import com.onatsubasi.finalcase.payment.domain.model.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.model.PaymentCancellation;
import com.onatsubasi.finalcase.payment.domain.model.PaymentRefund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentMapper {

    private final ObjectMapper objectMapper;
    private final PaymentServiceProperties properties;

    public Payment toPayment(InitializePaymentRequest request) {
        return new Payment(
                request.checkoutId(),
                request.orderId(),
                request.orderNumber(),
                request.userId(),
                request.provider(),
                request.method(),
                request.amount(),
                request.amount(),
                request.currency()
        );
    }

    public PaymentAttempt toPaymentAttempt(
            InitializePaymentRequest request,
            String idempotencyKey,
            String requestHash,
            int attemptNumber
    ) {
        return new PaymentAttempt(
                attemptNumber,
                idempotencyKey,
                requestHash,
                request.provider(),
                request.method(),
                request.amount(),
                request.amount(),
                request.currency()
        );
    }

    public ProviderPaymentInitializeCommand toProviderInitializeCommand(
            Payment payment,
            PaymentAttempt attempt,
            InitializePaymentRequest request
    ) {
        return ProviderPaymentInitializeCommand.builder()
                .paymentId(payment.getId())
                .paymentAttemptId(attempt.getId())
                .checkoutId(payment.getCheckoutId())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paidAmount(payment.getPaidAmount())
                .currency(payment.getCurrency())
                .provider(payment.getProvider())
                .method(payment.getMethod())
                .successUrl(request.successUrl())
                .failureUrl(request.failureUrl())
                .callbackUrl(properties.getCallbackUrl())
                .clientIp(request.clientIp())
                .basketId(request.basketId())
                .buyer(request.buyer())
                .shippingAddress(request.shippingAddress())
                .billingAddress(request.billingAddress())
                .basketItems(request.basketItems())
                .build();
    }

    public ProviderPaymentRetrieveCommand toProviderRetrieveCommand(
            PaymentAttempt attempt
    ) {
        return ProviderPaymentRetrieveCommand.builder()
                .provider(attempt.getProvider())
                .method(attempt.getMethod())
                .providerToken(attempt.getProviderToken())
                .paymentId(attempt.getPayment().getId())
                .paymentAttemptId(attempt.getId())
                .build();
    }

    public ProviderRefundCommand toProviderRefundCommand(
            Payment payment,
            PaymentRefund refund,
            RefundPaymentRequest request
    ) {
        return ProviderRefundCommand.builder()
                .paymentId(payment.getId())
                .refundId(refund.getId())
                .providerPaymentId(payment.getProviderPaymentId())
                .providerTransactionId(payment.getProviderTransactionId())
                .amount(request.amount())
                .currency(request.currency())
                .reason(request.reason())
                .build();
    }

    public ProviderCancelCommand toProviderCancelCommand(
            Payment payment,
            PaymentCancellation cancellation,
            CancelPaymentRequest request
    ) {
        return ProviderCancelCommand.builder()
                .paymentId(payment.getId())
                .cancellationId(cancellation.getId())
                .providerPaymentId(payment.getProviderPaymentId())
                .providerTransactionId(payment.getProviderTransactionId())
                .reason(request == null ? null : request.reason())
                .build();
    }

    public PaymentInitializeResponse toInitializeResponse(
            Payment payment,
            PaymentAttempt attempt
    ) {
        return new PaymentInitializeResponse(
                payment.getId(),
                attempt.getId(),
                payment.getOrderId(),
                payment.getCheckoutId(),
                payment.getProvider(),
                payment.getMethod(),
                payment.getStatus(),
                attempt.getStatus(),
                attempt.getProviderToken(),
                attempt.getPaymentPageUrl(),
                attempt.getCheckoutFormContent(),
                payment.getAmount(),
                payment.getPaidAmount(),
                payment.getCurrency()
        );
    }

    public PaymentSummaryResponse toSummaryResponse(Payment payment) {
        return new PaymentSummaryResponse(
                payment.getId(),
                payment.getCheckoutId(),
                payment.getOrderId(),
                payment.getOrderNumber(),
                payment.getUserId(),
                payment.getProvider(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getPaidAmount(),
                payment.getRefundedAmount(),
                payment.getCurrency(),
                payment.getCreatedAt(),
                payment.getCompletedAt()
        );
    }

    public PaymentDetailResponse toDetailResponse(Payment payment) {
        return new PaymentDetailResponse(
                payment.getId(),
                payment.getCheckoutId(),
                payment.getOrderId(),
                payment.getOrderNumber(),
                payment.getUserId(),
                payment.getProvider(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getPaidAmount(),
                payment.getRefundedAmount(),
                payment.getCurrency(),
                payment.getProviderPaymentId(),
                payment.getProviderTransactionId(),
                payment.getProviderConversationId(),
                payment.getProviderStatus(),
                payment.getFailureReason(),
                payment.getAttempts()
                        .stream()
                        .map(this::toAttemptResponse)
                        .toList(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getCompletedAt()
        );
    }

    public PaymentAttemptResponse toAttemptResponse(PaymentAttempt attempt) {
        return new PaymentAttemptResponse(
                attempt.getId(),
                attempt.getAttemptNumber(),
                attempt.getProvider(),
                attempt.getMethod(),
                attempt.getStatus(),
                attempt.getAmount(),
                attempt.getPaidAmount(),
                attempt.getCurrency(),
                attempt.getProviderToken(),
                attempt.getProviderPaymentId(),
                attempt.getProviderTransactionId(),
                attempt.getProviderConversationId(),
                attempt.getProviderStatus(),
                attempt.getPaymentPageUrl(),
                attempt.getFailureReason(),
                attempt.getCreatedAt(),
                attempt.getCompletedAt()
        );
    }

    public PaymentRefundResponse toRefundResponse(PaymentRefund refund) {
        return new PaymentRefundResponse(
                refund.getId(),
                refund.getPayment().getId(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getStatus(),
                refund.getProviderRefundId(),
                refund.getProviderStatus(),
                refund.getFailureReason(),
                refund.getCreatedAt(),
                refund.getCompletedAt()
        );
    }

    public PaymentCancellationResponse toCancellationResponse(
            PaymentCancellation cancellation
    ) {
        return new PaymentCancellationResponse(
                cancellation.getId(),
                cancellation.getPayment().getId(),
                cancellation.getStatus(),
                cancellation.getProviderCancelId(),
                cancellation.getProviderStatus(),
                cancellation.getFailureReason(),
                cancellation.getCreatedAt(),
                cancellation.getCompletedAt()
        );
    }

    public PaymentResultEvent toPaymentResultEvent(Payment payment) {
        return PaymentResultEvent.builder()
                .paymentId(payment.getId())
                .checkoutId(payment.getCheckoutId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .provider(payment.getProvider())
                .method(payment.getMethod())
                .paymentStatus(payment.getStatus())
                .providerPaymentId(payment.getProviderPaymentId())
                .providerTransactionId(payment.getProviderTransactionId())
                .providerConversationId(payment.getProviderConversationId())
                .amount(payment.getAmount())
                .paidAmount(payment.getPaidAmount())
                .currency(payment.getCurrency())
                .failureReason(payment.getFailureReason())
                .build();
    }

    public PaymentRefundedEvent toPaymentRefundedEvent(
            Payment payment,
            PaymentRefund refund
    ) {
        return PaymentRefundedEvent.builder()
                .paymentId(payment.getId())
                .refundId(refund.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .refundAmount(refund.getAmount())
                .totalRefundedAmount(payment.getRefundedAmount())
                .currency(payment.getCurrency())
                .paymentStatus(payment.getStatus())
                .build();
    }

    public PaymentCancelledEvent toPaymentCancelledEvent(
            Payment payment,
            PaymentCancellation cancellation
    ) {
        return PaymentCancelledEvent.builder()
                .paymentId(payment.getId())
                .cancellationId(cancellation.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .paymentStatus(payment.getStatus())
                .build();
    }

    public Map<String, Object> toMap(Object value) {
        if (value == null) {
            return new HashMap<>();
        }

        return objectMapper.convertValue(
                value,
                new TypeReference<Map<String, Object>>() {
                }
        );
    }
}