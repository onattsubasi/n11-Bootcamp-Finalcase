package com.onatsubasi.finalcase.search.application.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryStockUpdatedMessage(
        String productId,
        int totalQuantity,
        int reservedQuantity,
        int availableQuantity
) {
}