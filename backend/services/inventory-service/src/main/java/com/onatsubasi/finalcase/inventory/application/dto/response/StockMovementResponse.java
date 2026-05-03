package com.onatsubasi.finalcase.inventory.application.dto.response;

import com.onatsubasi.finalcase.inventory.domain.enums.StockMovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Stock movement audit response")
public record StockMovementResponse(
        UUID id,
        UUID inventoryItemId,
        UUID productId,
        StockMovementType movementType,
        int quantityChange,
        int totalBefore,
        int reservedBefore,
        int totalAfter,
        int reservedAfter,
        UUID reservationId,
        UUID checkoutId,
        UUID orderId,
        String reason,
        String referenceId,
        Instant occurredAt
) {
}
