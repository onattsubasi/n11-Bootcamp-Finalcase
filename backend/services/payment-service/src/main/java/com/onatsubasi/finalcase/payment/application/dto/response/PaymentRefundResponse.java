package com.onatsubasi.finalcase.payment.application.dto.response;

import com.onatsubasi.finalcase.payment.domain.enums.RefundStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Payment refund response")
public record PaymentRefundResponse(
        UUID id,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        RefundStatus status,
        String providerRefundId,
        String providerStatus,
        String failureReason,
        Instant createdAt,
        Instant completedAt
) {
}