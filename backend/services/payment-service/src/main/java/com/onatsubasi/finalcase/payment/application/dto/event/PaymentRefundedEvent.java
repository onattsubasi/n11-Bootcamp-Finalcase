package com.onatsubasi.finalcase.payment.application.dto.event;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentRefundedEvent(
        UUID paymentId,
        UUID refundId,
        UUID orderId,
        UUID userId,
        BigDecimal refundAmount,
        BigDecimal totalRefundedAmount,
        String currency,
        PaymentStatus paymentStatus
) {
}