package com.onatsubasi.finalcase.promotion.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request to create a coupon")
public record CreateCouponRequest(

        @NotNull(message = "promotionId is required")
        UUID promotionId,

        @NotBlank(message = "code is required")
        @Size(max = 80)
        String code,

        @Min(value = 1, message = "usageLimit must be greater than zero")
        Integer usageLimit,

        @Min(value = 1, message = "perUserUsageLimit must be greater than zero")
        Integer perUserUsageLimit,

        Instant startsAt,

        Instant endsAt
) {
}