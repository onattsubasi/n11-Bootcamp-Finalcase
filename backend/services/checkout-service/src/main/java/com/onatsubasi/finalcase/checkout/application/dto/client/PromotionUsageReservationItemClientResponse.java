package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.UUID;

public record PromotionUsageReservationItemClientResponse(
        UUID id,
        UUID promotionId,
        UUID couponId,
        String couponCode,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount,
        String description
) {
}