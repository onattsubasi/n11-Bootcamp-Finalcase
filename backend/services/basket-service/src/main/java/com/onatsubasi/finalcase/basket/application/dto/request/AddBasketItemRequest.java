package com.onatsubasi.finalcase.basket.application.dto.request;

import com.onatsubasi.finalcase.basket.domain.entity.BasketItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to add a product to the active basket")
public record AddBasketItemRequest(

        @NotNull(message = "productId is required")
        @Schema(
                description = "Product UUID to add to basket",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID productId,

        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = BasketItem.MAX_QUANTITY, message = "quantity cannot exceed 99")
        @Schema(
                description = "Quantity to add",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int quantity
) {
}