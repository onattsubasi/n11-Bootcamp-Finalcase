package com.onatsubasi.finalcase.promotion.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request to create a batch of generated coupons")
public record CreateCouponBatchRequest(

        @NotNull(message = "promotionId is required")
        UUID promotionId,

        @NotBlank(message = "codePrefix is required")
        @Size(max = 30)
        String codePrefix,

        @Min(value = 1, message = "count must be at least 1")
        @Max(value = 500, message = "count cannot exceed 500")
        int count,

        @Min(value = 1, message = "usageLimit must be greater than zero")
        Integer usageLimit,

        @Min(value = 1, message = "perUserUsageLimit must be greater than zero")
        Integer perUserUsageLimit,

        Instant startsAt,

        Instant endsAt
) {
}
