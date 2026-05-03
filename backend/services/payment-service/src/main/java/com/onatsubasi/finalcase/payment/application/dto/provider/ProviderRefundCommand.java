package com.onatsubasi.finalcase.payment.application.dto.provider;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProviderRefundCommand(
        UUID paymentId,
        UUID refundId,
        String providerPaymentId,
        String providerTransactionId,
        BigDecimal amount,
        String currency,
        String reason
) {
}