package com.onatsubasi.finalcase.promotion.application.dto.response;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageCancelReason;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromotionUsageReservationResponse(
        UUID id,
        String idempotencyKey,
        UUID checkoutId,
        UUID userId,
        UUID orderId,
        PromotionUsageReservationStatus status,
        Instant reservedUntil,
        Instant redeemedAt,
        Instant cancelledAt,
        Instant expiredAt,
        PromotionUsageCancelReason cancelReason,
        List<PromotionUsageReservationItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}