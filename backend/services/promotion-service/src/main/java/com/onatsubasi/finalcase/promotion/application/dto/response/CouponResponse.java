package com.onatsubasi.finalcase.promotion.application.dto.response;

import com.onatsubasi.finalcase.promotion.domain.enums.CouponStatus;

import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
        UUID id,
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
}