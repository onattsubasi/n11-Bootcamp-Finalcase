package com.onatsubasi.finalcase.shipment.application.dto.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDetailClientResponse(
        UUID id,
        String orderNumber,
        UUID checkoutId,
        UUID userId,
        UUID basketId,
        UUID inventoryReservationId,
        UUID promotionUsageReservationId,
        String status,

        OrderAddressClientResponse shippingAddress,
        OrderAddressClientResponse billingAddress,

        BigDecimal subtotalAmount,
        BigDecimal itemDiscountAmount,
        BigDecimal promotionDiscountAmount,
        BigDecimal shippingFee,
        BigDecimal shippingDiscountAmount,
        BigDecimal taxAmount,
        BigDecimal grandTotalAmount,
        String currency,

        List<OrderItemClientResponse> items,

        Instant createdAt,
        Instant updatedAt
) {
}