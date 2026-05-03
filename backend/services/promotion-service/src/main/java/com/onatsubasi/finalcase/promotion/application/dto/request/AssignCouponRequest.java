package com.onatsubasi.finalcase.promotion.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request to assign a coupon to a customer")
public record AssignCouponRequest(

        @NotNull(message = "couponId is required")
        UUID couponId,

        @NotNull(message = "userId is required")
        UUID userId,

        Instant expiresAt
) {
}