package com.onatsubasi.finalcase.order.application.dto.response;

import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID id,
        String orderNumber,
        UUID checkoutId,
        UUID userId,
        UUID basketId,
        UUID inventoryReservationId,
        UUID promotionUsageReservationId,
        OrderStatus status,

        OrderAddressSnapshotResponse shippingAddress,
        OrderAddressSnapshotResponse billingAddress,

        OrderPaymentSummaryResponse paymentSummary,
        OrderShipmentSummaryResponse shipmentSummary,

        BigDecimal subtotalAmount,
        BigDecimal itemDiscountAmount,
        BigDecimal promotionDiscountAmount,
        BigDecimal shippingFee,
        BigDecimal shippingDiscountAmount,
        BigDecimal taxAmount,
        BigDecimal grandTotalAmount,
        String currency,

        List<OrderItemResponse> items,
        List<OrderDiscountResponse> discounts,
        List<OrderStatusHistoryResponse> statusHistory,

        Instant createdAt,
        Instant updatedAt
) {
}