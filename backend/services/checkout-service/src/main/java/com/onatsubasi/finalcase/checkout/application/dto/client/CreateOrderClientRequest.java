package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderClientRequest(
        UUID checkoutId,
        String idempotencyKey,
        String requestHash,
        UUID userId,
        UUID basketId,
        UUID inventoryReservationId,
        UUID promotionUsageReservationId,

        OrderAddressSnapshotClientRequest shippingAddress,
        OrderAddressSnapshotClientRequest billingAddress,

        BigDecimal subtotalAmount,
        BigDecimal itemDiscountAmount,
        BigDecimal promotionDiscountAmount,
        BigDecimal shippingFee,
        BigDecimal shippingDiscountAmount,
        BigDecimal taxAmount,
        BigDecimal grandTotalAmount,
        String currency,

        List<CreateOrderItemClientRequest> items,
        List<CreateOrderDiscountClientRequest> discounts
) {
}