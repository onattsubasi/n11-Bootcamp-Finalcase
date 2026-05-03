package com.onatsubasi.finalcase.inventory.application.dto.response;

public record StockItemResponse(
        String productId,
        int totalQuantity,
        int reservedQuantity,
        int availableQuantity
) {
}