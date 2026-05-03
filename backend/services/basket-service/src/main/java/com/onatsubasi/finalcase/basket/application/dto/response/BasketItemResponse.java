package com.onatsubasi.finalcase.basket.application.dto.response;

import com.onatsubasi.finalcase.basket.domain.enums.BasketItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Basket item response")
public record BasketItemResponse(

        @Schema(description = "Basket item id")
        UUID basketItemId,

        @Schema(description = "Product id")
        UUID productId,

        @Schema(description = "Selected quantity", example = "2")
        int quantity,

        @Schema(description = "Basket item status", example = "ACTIVE")
        BasketItemStatus itemStatus,

        @Schema(description = "Optional stale/unavailable reason")
        String staleReason,

        @Schema(description = "Optional product name snapshot for UX only")
        String productNameSnapshot,

        @Schema(description = "Optional product image URL snapshot for UX only")
        String imageUrlSnapshot,

        @Schema(description = "Optional unit price snapshot for UX only. Not authoritative for checkout.")
        BigDecimal unitPriceSnapshot,

        @Schema(description = "Optional snapshot currency")
        String snapshotCurrency,

        @Schema(description = "Item creation time")
        Instant addedAt,

        @Schema(description = "Item last update time")
        Instant updatedAt
) {
}