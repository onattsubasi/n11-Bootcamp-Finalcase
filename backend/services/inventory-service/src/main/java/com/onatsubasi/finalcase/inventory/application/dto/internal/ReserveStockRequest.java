package com.onatsubasi.finalcase.inventory.application.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Internal request to reserve stock during checkout")
public record ReserveStockRequest(

        @NotNull(message = "checkoutId is required")
        @Schema(description = "Checkout session id", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID checkoutId,

        @NotNull(message = "userId is required")
        @Schema(description = "Customer user id", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID userId,

        @NotEmpty(message = "items cannot be empty")
        @Valid
        @Schema(description = "Requested stock reservation items", requiredMode = Schema.RequiredMode.REQUIRED)
        List<ReserveStockItemRequest> items
) {
}
