package com.onatsubasi.finalcase.basket.application.dto.response;

import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Basket response")
public record BasketResponse(

        @Schema(description = "Basket id")
        UUID basketId,

        @Schema(description = "Basket owner user id")
        UUID userId,

        @Schema(description = "Basket status", example = "ACTIVE")
        BasketStatus status,

        @Schema(
                description = "Customer-entered coupon code candidate. Basket does not validate or price this coupon.",
                example = "WELCOME10"
        )
        String couponCodeIntent,

        @Schema(description = "Basket items")
        List<BasketItemResponse> items,

        @Schema(description = "Number of distinct basket items", example = "2")
        int itemCount,

        @Schema(description = "Total quantity across all items", example = "5")
        int totalQuantity,

        @Schema(description = "Whether basket has no items", example = "false")
        boolean empty,

        @Schema(description = "Basket last update time")
        Instant updatedAt
) {
}