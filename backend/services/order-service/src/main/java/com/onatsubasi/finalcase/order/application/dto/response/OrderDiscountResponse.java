package com.onatsubasi.finalcase.order.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderDiscountResponse(
        UUID id,
        UUID promotionId,
        String promotionName,
        UUID couponId,
        String couponCode,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount
) {
}