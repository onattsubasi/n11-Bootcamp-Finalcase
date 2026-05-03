package com.onatsubasi.finalcase.promotion.application.dto.response;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;

import java.math.BigDecimal;
import java.util.UUID;

public record AppliedDiscountResponse(
        UUID promotionId,
        UUID couponId,
        UUID couponAssignmentId,
        String couponCode,
        PromotionType promotionType,
        String description,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount,
        BigDecimal totalDiscountAmount
) {
}