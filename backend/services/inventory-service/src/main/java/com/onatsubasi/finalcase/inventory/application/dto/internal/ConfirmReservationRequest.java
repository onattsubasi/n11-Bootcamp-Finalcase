package com.onatsubasi.finalcase.inventory.application.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Internal request to confirm a stock reservation after payment success")
public record ConfirmReservationRequest(

        @NotNull(message = "orderId is required")
        @Schema(description = "Order id created by Order Service", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID orderId
) {
}
