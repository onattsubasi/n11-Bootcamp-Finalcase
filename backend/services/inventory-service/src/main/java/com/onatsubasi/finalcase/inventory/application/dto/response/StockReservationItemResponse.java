package com.onatsubasi.finalcase.inventory.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Stock reservation item response")
public record StockReservationItemResponse(
        UUID id,
        UUID productId,
        int quantity,
        Instant createdAt
) {
}
