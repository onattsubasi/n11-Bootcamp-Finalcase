package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PromotionUsageReservationItemClientResponse(
        UUID id,
        UUID promotionId,
        UUID couponId,
        UUID couponAssignmentId,
        String couponCode,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount,
        BigDecimal totalDiscountAmount,
        String description
) {
}
