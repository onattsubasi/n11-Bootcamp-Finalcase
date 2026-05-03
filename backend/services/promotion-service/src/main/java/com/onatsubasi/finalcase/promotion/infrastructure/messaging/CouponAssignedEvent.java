package com.onatsubasi.finalcase.promotion.infrastructure.messaging;

import com.onatsubasi.finalcase.promotion.domain.enums.CouponAssignmentStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;

import java.time.Instant;
import java.util.UUID;

public record CouponAssignedEvent(
        UUID assignmentId,
        UUID couponId,
        String couponCode,
        UUID promotionId,
        UUID userId,
        CouponAssignmentStatus status,
        Instant assignedAt,
        Instant expiresAt
) {

    public static CouponAssignedEvent from(CouponAssignment assignment) {
        Coupon coupon = assignment.getCoupon();
        Promotion promotion = coupon.getPromotion();

        return new CouponAssignedEvent(
                assignment.getId(),
                coupon.getId(),
                coupon.getCode(),
                promotion.getId(),
                assignment.getUserId(),
                assignment.getStatus(),
                assignment.getAssignedAt(),
                assignment.getExpiresAt()
        );
    }
}