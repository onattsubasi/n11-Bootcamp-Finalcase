package com.onatsubasi.finalcase.inventory.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to create an inventory item for a product")
public record CreateInventoryItemRequest(

        @NotNull(message = "productId is required")
        @Schema(
                description = "Product id from Catalog Service",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID productId,

        @Min(value = 0, message = "initialQuantity cannot be negative")
        @Schema(
                description = "Initial total stock quantity",
                example = "100"
        )
        int initialQuantity,

        @Min(value = 0, message = "lowStockThreshold cannot be negative")
        @Schema(
                description = "Threshold used to classify LOW_STOCK",
                example = "5"
        )
        int lowStockThreshold
) {
}