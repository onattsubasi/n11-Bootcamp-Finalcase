package com.onatsubasi.finalcase.payment.application.dto.provider;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentAttemptStatus;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import lombok.Builder;

import java.util.Map;

@Builder
public record ProviderPaymentInitializeResult(
        boolean success,
        PaymentAttemptStatus attemptStatus,
        PaymentStatus paymentStatus,
        String providerToken,
        String providerPaymentId,
        String providerTransactionId,
        String providerConversationId,
        String providerStatus,
        String paymentPageUrl,
        String checkoutFormContent,
        String failureReason,
        Map<String, Object> providerResponseSnapshot
) {
}
