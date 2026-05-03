package com.onatsubasi.finalcase.promotion.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromotionUsageReservationItemResponse(
        UUID id,
        UUID promotionId,
        UUID couponId,
        UUID couponAssignmentId,
        String couponCode,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount,
        BigDecimal totalDiscountAmount,
        String description,
        Instant createdAt
) {
}