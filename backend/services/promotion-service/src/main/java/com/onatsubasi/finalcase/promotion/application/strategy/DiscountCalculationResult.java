package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;

import java.math.BigDecimal;
import java.util.UUID;

public record DiscountCalculationResult(
        UUID promotionId,
        PromotionType promotionType,
        String description,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount
) {

    public BigDecimal totalDiscountAmount() {
        return discountAmount.add(shippingDiscountAmount);
    }

    public boolean hasDiscount() {
        return totalDiscountAmount().signum() > 0;
    }
}
