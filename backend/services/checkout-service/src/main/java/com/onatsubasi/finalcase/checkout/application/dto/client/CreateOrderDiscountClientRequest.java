package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderDiscountClientRequest(
        UUID promotionId,
        String promotionName,
        UUID couponId,
        String couponCode,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount
) {
}