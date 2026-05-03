package com.onatsubasi.finalcase.promotion.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Request to update a promotion")
public record UpdatePromotionRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        boolean couponRequired,

        boolean stackable,

        @Min(value = 0, message = "priority cannot be negative")
        int priority,

        @NotNull(message = "ruleConfig is required")
        Map<String, Object> ruleConfig,

        @Min(value = 1, message = "globalUsageLimit must be greater than zero")
        Integer globalUsageLimit,

        @Min(value = 1, message = "perUserUsageLimit must be greater than zero")
        Integer perUserUsageLimit,

        @NotNull(message = "startsAt is required")
        Instant startsAt,

        Instant endsAt
) {
}