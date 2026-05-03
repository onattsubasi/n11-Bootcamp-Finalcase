package com.onatsubasi.finalcase.basket.application.dto.internal;

import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Internal response returned after basket is marked checked out")
public record MarkBasketCheckedOutResponse(

        @Schema(description = "Basket id")
        UUID basketId,

        @Schema(description = "Order id associated with checked-out basket")
        UUID orderId,

        @Schema(description = "Basket status", example = "CHECKED_OUT")
        BasketStatus status,

        @Schema(description = "Checkout timestamp")
        Instant checkedOutAt
) {
}