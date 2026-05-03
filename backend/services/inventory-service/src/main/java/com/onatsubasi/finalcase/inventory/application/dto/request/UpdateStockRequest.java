package com.onatsubasi.finalcase.inventory.application.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateStockRequest(
        @Min(0)
        int totalQuantity
) {
}