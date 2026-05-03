package com.onatsubasi.finalcase.promotion.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.time.Instant;

@Schema(description = "Request to update coupon limits and date window")
public record UpdateCouponRequest(

        @Min(value = 1, message = "usageLimit must be greater than zero")
        Integer usageLimit,

        @Min(value = 1, message = "perUserUsageLimit must be greater than zero")
        Integer perUserUsageLimit,

        Instant startsAt,

        Instant endsAt
) {
}
