package com.onatsubasi.finalcase.payment.application.dto.event;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentCancelledEvent(
        UUID paymentId,
        UUID cancellationId,
        UUID orderId,
        UUID userId,
        PaymentStatus paymentStatus
) {
}