package com.onatsubasi.finalcase.payment.application.dto.response;

import com.onatsubasi.finalcase.payment.domain.enums.CancellationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Payment cancellation response")
public record PaymentCancellationResponse(
        UUID id,
        UUID paymentId,
        CancellationStatus status,
        String providerCancelId,
        String providerStatus,
        String failureReason,
        Instant createdAt,
        Instant completedAt
) {
}