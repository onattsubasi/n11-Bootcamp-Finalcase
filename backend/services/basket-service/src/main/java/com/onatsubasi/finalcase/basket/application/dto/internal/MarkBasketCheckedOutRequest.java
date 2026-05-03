package com.onatsubasi.finalcase.basket.application.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Internal request to mark basket as checked out")
public record MarkBasketCheckedOutRequest(

        @NotNull(message = "orderId is required")
        @Schema(
                description = "Order id created by Order Service",
                example = "9fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID orderId
) {
}