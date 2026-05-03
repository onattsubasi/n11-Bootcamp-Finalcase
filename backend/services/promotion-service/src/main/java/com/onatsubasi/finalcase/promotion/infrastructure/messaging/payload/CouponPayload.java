package com.onatsubasi.finalcase.promotion.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.promotion.domain.enums.CouponStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;

import java.time.Instant;
import java.util.UUID;

public record CouponPayload(
        UUID couponId,
        UUID promotionId,
        String code,
        CouponStatus status,
        Integer usageLimit,
        Integer perUserUsageLimit,
        int reservedUsageCount,
        int redeemedUsageCount,
        Instant startsAt,
        Instant endsAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static CouponPayload from(Coupon coupon) {
        return new CouponPayload(
                coupon.getId(),
                coupon.getPromotion().getId(),
                coupon.getCode(),
                coupon.getStatus(),
                coupon.getUsageLimit(),
                coupon.getPerUserUsageLimit(),
                coupon.getReservedUsageCount(),
                coupon.getRedeemedUsageCount(),
                coupon.getStartsAt(),
                coupon.getEndsAt(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }
}
