package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppliedPromotionDiscountClientResponse(
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
