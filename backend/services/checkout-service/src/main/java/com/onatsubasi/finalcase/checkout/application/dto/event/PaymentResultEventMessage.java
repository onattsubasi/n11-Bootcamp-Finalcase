package com.onatsubasi.finalcase.checkout.application.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentResultEventMessage(
        UUID paymentId,
        UUID checkoutId,
        UUID orderId,
        UUID userId,
        String provider,
        String paymentStatus,
        String providerTransactionId,
        BigDecimal amount,
        String currency,
        String failureReason
) {
}