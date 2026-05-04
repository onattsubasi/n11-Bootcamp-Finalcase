package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderClientResponse(
        UUID id,
        String orderNumber,
        UUID checkoutId,
        UUID userId,
        UUID basketId,
        UUID inventoryReservationId,
        UUID promotionUsageReservationId,
        String status,
        BigDecimal subtotalAmount,
        BigDecimal itemDiscountAmount,
        BigDecimal promotionDiscountAmount,
        BigDecimal shippingFee,
        BigDecimal shippingDiscountAmount,
        BigDecimal taxAmount,
        BigDecimal grandTotalAmount,
        String currency
) {
}
