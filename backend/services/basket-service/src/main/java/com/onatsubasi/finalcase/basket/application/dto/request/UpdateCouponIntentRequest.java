package com.onatsubasi.finalcase.basket.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update customer's coupon code intent")
public record UpdateCouponIntentRequest(

        @Size(max = 80, message = "couponCodeIntent cannot exceed 80 characters")
        @Schema(
                description = "Customer-entered coupon code candidate. Basket stores it as intent only; Promotion validates it.",
                example = "WELCOME10"
        )
        String couponCodeIntent
) {
}