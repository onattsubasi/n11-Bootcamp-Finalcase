package com.onatsubasi.finalcase.inventory.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to increase product stock")
public record IncreaseStockRequest(

        @Min(value = 1, message = "quantity must be greater than zero")
        @Schema(description = "Quantity to add", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity,

        @Size(max = 500)
        @Schema(description = "Optional admin reason", example = "Supplier delivery")
        String reason
) {
}