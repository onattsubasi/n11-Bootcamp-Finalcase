package com.onatsubasi.finalcase.promotion.application.dto.response;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionStatus;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PromotionResponse(
        UUID id,
        String name,
        String description,
        PromotionType type,
        PromotionStatus status,
        boolean couponRequired,
        boolean stackable,
        int priority,
        Map<String, Object> ruleConfig,
        Integer globalUsageLimit,
        Integer perUserUsageLimit,
        int reservedUsageCount,
        int redeemedUsageCount,
        Instant startsAt,
        Instant endsAt,
        Instant createdAt,
        Instant updatedAt
) {
}