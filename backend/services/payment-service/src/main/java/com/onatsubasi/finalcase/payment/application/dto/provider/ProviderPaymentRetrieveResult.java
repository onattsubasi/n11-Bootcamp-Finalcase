package com.onatsubasi.finalcase.payment.application.dto.provider;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentAttemptStatus;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
public record ProviderPaymentRetrieveResult(
        boolean success,
        PaymentStatus paymentStatus,
        PaymentAttemptStatus attemptStatus,
        String providerPaymentId,
        String providerTransactionId,
        String providerConversationId,
        String providerStatus,
        String failureReason,
        BigDecimal paidAmount,
        String currency,
        Map<String, Object> providerResponseSnapshot
) {
}