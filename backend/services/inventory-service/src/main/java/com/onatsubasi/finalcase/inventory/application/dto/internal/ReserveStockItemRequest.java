package com.onatsubasi.finalcase.inventory.application.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Stock reservation item request")
public record ReserveStockItemRequest(

        @NotNull(message = "productId is required")
        @Schema(description = "Product id", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID productId,

        @Min(value = 1, message = "quantity must be greater than zero")
        @Schema(description = "Quantity to reserve", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity
) {
}
