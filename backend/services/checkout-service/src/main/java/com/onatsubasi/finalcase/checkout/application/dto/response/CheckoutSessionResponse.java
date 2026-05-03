package com.onatsubasi.finalcase.checkout.application.dto.response;

import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CheckoutSessionResponse(
        UUID id,
        UUID userId,
        UUID basketId,
        String idempotencyKey,
        CheckoutStatus status,

        UUID inventoryReservationId,
        UUID promotionUsageReservationId,
        UUID orderId,
        String orderNumber,
        UUID paymentId,
        String paymentSessionId,
        String paymentRedirectUrl,
        UUID shipmentId,

        CheckoutMoneyBreakdownResponse money,
        Map<String, Object> quoteSnapshot,
        Map<String, Object> paymentActionSnapshot,
        List<CheckoutSagaStepResponse> sagaSteps,

        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {
}