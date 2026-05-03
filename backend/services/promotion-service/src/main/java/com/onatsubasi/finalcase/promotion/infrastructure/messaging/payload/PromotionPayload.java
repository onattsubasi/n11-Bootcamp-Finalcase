package com.onatsubasi.finalcase.promotion.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionStatus;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PromotionPayload(
        UUID promotionId,
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

    public static PromotionPayload from(Promotion promotion) {
        return new PromotionPayload(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getType(),
                promotion.getStatus(),
                promotion.isCouponRequired(),
                promotion.isStackable(),
                promotion.getPriority(),
                promotion.getRuleConfig(),
                promotion.getGlobalUsageLimit(),
                promotion.getPerUserUsageLimit(),
                promotion.getReservedUsageCount(),
                promotion.getRedeemedUsageCount(),
                promotion.getStartsAt(),
                promotion.getEndsAt(),
                promotion.getCreatedAt(),
                promotion.getUpdatedAt()
        );
    }
}
