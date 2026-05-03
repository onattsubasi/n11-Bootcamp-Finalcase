package com.onatsubasi.finalcase.inventory.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to set product total stock")
public record SetStockRequest(

        @Min(value = 0, message = "totalQuantity cannot be negative")
        @Schema(description = "New total stock quantity", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
        int totalQuantity,

        @Size(max = 500)
        @Schema(description = "Optional admin reason", example = "Manual stock reconciliation")
        String reason
) {
}
