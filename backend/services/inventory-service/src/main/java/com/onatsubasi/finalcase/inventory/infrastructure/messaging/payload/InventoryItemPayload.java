package com.onatsubasi.finalcase.inventory.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.inventory.domain.enums.InventoryItemStatus;
import com.onatsubasi.finalcase.inventory.domain.enums.StockStatus;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;

import java.time.Instant;
import java.util.UUID;

public record InventoryItemPayload(
        UUID inventoryItemId,
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

    public static InventoryItemPayload from(InventoryItem item) {
        return new InventoryItemPayload(
                item.getId(),
                item.getProductId(),
                item.getTotalQuantity(),
                item.getReservedQuantity(),
                item.availableQuantity(),
                item.getLowStockThreshold(),
                item.getStatus(),
                item.stockStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}