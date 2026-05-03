package com.onatsubasi.finalcase.inventory.application.dto.response;

import com.onatsubasi.finalcase.inventory.domain.enums.InventoryItemStatus;
import com.onatsubasi.finalcase.inventory.domain.enums.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Inventory item response")
public record InventoryItemResponse(
        UUID id,
        UUID productId,
        int totalQuantity,
        int reservedQuantity,
        int availableQuantity,
        int lowStockThreshold,
        InventoryItemStatus status,
        StockStatus stockStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
