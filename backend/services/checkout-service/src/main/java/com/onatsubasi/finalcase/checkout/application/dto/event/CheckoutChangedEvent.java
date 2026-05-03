package com.onatsubasi.finalcase.checkout.application.dto.event;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutChangedEvent(
        UUID checkoutId,
        UUID userId,
        UUID basketId,
        UUID orderId,
        String orderNumber,
        UUID paymentId,
        UUID shipmentId,
        CheckoutStatus status,
        BigDecimal grandTotalAmount,
        String currency
) {

    public static CheckoutChangedEvent from(CheckoutSession session) {
        return new CheckoutChangedEvent(
                session.getId(),
                session.getUserId(),
                session.getBasketId(),
                session.getOrderId(),
                session.getOrderNumber(),
                session.getPaymentId(),
                session.getShipmentId(),
                session.getStatus(),
                session.getGrandTotalAmount(),
                session.getCurrency()
        );
    }
}