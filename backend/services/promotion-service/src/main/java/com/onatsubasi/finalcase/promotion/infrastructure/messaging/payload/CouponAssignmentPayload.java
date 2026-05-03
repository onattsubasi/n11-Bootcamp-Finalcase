package com.onatsubasi.finalcase.promotion.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.promotion.domain.enums.CouponAssignmentStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;

import java.time.Instant;
import java.util.UUID;

public record CouponAssignmentPayload(
        UUID assignmentId,
        UUID couponId,
        String couponCode,
        UUID promotionId,
        UUID userId,
        CouponAssignmentStatus status,
        Instant assignedAt,
        Instant expiresAt,
        Instant reservedAt,
        Instant redeemedAt,
        Instant cancelledAt,
        Instant expiredAt,
        Instant updatedAt
) {

    public static CouponAssignmentPayload from(CouponAssignment assignment) {
        Coupon coupon = assignment.getCoupon();

        return new CouponAssignmentPayload(
                assignment.getId(),
                coupon.getId(),
                coupon.getCode(),
                coupon.getPromotion().getId(),
                assignment.getUserId(),
                assignment.getStatus(),
                assignment.getAssignedAt(),
                assignment.getExpiresAt(),
                assignment.getReservedAt(),
                assignment.getRedeemedAt(),
                assignment.getCancelledAt(),
                assignment.getExpiredAt(),
                assignment.getUpdatedAt()
        );
    }
}
