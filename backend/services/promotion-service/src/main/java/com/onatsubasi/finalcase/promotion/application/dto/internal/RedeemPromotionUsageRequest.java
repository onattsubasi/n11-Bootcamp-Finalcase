package com.onatsubasi.finalcase.promotion.application.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Internal request to redeem reserved promotion usage after payment success")
public record RedeemPromotionUsageRequest(

        @NotNull(message = "orderId is required")
        UUID orderId
) {
}
