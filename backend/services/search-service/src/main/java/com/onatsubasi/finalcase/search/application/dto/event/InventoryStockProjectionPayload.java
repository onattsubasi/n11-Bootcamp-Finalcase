package com.onatsubasi.finalcase.search.application.dto.event;

import com.onatsubasi.finalcase.search.domain.enums.StockStatus;

import java.time.Instant;
import java.util.UUID;

public record InventoryStockProjectionPayload(
        UUID productId,
        int availableQuantity,
        StockStatus stockStatus,
        Instant stockUpdatedAt
) {
}
