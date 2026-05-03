package com.onatsubasi.finalcase.checkout.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutDiscountResponse(
        UUID promotionId,
        String promotionName,
        UUID couponId,
        String couponCode,
        String promotionType,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount,
        String description
) {
}
