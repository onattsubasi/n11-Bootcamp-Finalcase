package com.onatsubasi.finalcase.inventory.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to decrease product stock")
public record DecreaseStockRequest(

        @Min(value = 1, message = "quantity must be greater than zero")
        @Schema(description = "Quantity to subtract", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity,

        @Size(max = 500)
        @Schema(description = "Optional admin reason", example = "Damaged stock removed")
        String reason
) {
}