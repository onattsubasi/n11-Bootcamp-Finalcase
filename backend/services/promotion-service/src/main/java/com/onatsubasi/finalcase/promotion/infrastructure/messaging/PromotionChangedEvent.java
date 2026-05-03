package com.onatsubasi.finalcase.promotion.infrastructure.messaging;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionStatus;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;

import java.util.UUID;

public record PromotionChangedEvent(
        UUID promotionId,
        String name,
        PromotionType type,
        PromotionStatus status,
        boolean couponRequired,
        boolean stackable
) {

    public static PromotionChangedEvent from(Promotion promotion) {
        return new PromotionChangedEvent(
                promotion.getId(),
                promotion.getName(),
                promotion.getType(),
                promotion.getStatus(),
                promotion.isCouponRequired(),
                promotion.isStackable()
        );
    }
}