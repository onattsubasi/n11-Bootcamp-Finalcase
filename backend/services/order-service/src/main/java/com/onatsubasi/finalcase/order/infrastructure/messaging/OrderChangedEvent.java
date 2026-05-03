package com.onatsubasi.finalcase.order.infrastructure.messaging;

import com.onatsubasi.finalcase.order.domain.entity.Order;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderChangedEvent(
        UUID orderId,
        String orderNumber,
        UUID checkoutId,
        UUID userId,
        OrderStatus status,
        BigDecimal grandTotalAmount,
        String currency
) {

    public static OrderChangedEvent from(Order order) {
        return new OrderChangedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getCheckoutId(),
                order.getUserId(),
                order.getStatus(),
                order.getGrandTotalAmount(),
                order.getCurrency()
        );
    }
}