package com.onatsubasi.finalcase.promotion.application.dto.response;

import com.onatsubasi.finalcase.promotion.domain.enums.CouponAssignmentStatus;

import java.time.Instant;
import java.util.UUID;

public record CouponAssignmentResponse(
        UUID id,
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
}