package com.onatsubasi.finalcase.inventory.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Request to update low stock threshold")
public record UpdateLowStockThresholdRequest(

        @Min(value = 0, message = "lowStockThreshold cannot be negative")
        @Schema(description = "New low stock threshold", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        int lowStockThreshold
) {
}
