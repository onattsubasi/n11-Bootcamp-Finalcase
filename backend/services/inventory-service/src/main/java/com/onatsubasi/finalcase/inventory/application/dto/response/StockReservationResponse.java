package com.onatsubasi.finalcase.inventory.application.dto.response;

import com.onatsubasi.finalcase.inventory.domain.enums.ReleaseReason;
import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Stock reservation response")
public record StockReservationResponse(
        UUID id,
        String idempotencyKey,
        UUID checkoutId,
        UUID userId,
        UUID orderId,
        StockReservationStatus status,
        Instant reservedUntil,
        Instant confirmedAt,
        Instant releasedAt,
        ReleaseReason releaseReason,
        List<StockReservationItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}