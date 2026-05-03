package com.onatsubasi.finalcase.inventory.application.dto.event;

import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;

import java.util.UUID;

public record StockReservationChangedEvent(
        UUID reservationId,
        UUID orderId
) {

    public static StockReservationChangedEvent from(StockReservation reservation) {
        return new StockReservationChangedEvent(
                reservation.getId(),
                reservation.getOrderId()
        );
    }
}