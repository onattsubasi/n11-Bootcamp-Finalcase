package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.UUID;

public record AppliedPromotionDiscountClientResponse(
        UUID promotionId,
        UUID couponId,
        String couponCode,
        String promotionType,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount,
        String description
) {
}
