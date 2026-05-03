package com.onatsubasi.finalcase.inventory.application.dto.event;

import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;

public record StockUpdatedEvent(
        String productId,
        int totalQuantity,
        int reservedQuantity,
        int availableQuantity) {
    public static StockUpdatedEvent from(InventoryItem item) {
        return new StockUpdatedEvent(
                item.getProductId().toString(),
                item.getTotalQuantity(),
                item.getReservedQuantity(),
                item.availableQuantity());
    }
}