package com.onatsubasi.finalcase.basket.application.dto.request;

import com.onatsubasi.finalcase.basket.domain.entity.BasketItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Request to update basket item quantity")
public record UpdateBasketItemQuantityRequest(

        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = BasketItem.MAX_QUANTITY, message = "quantity cannot exceed 99")
        @Schema(
                description = "New quantity",
                example = "3",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int quantity
) {
}