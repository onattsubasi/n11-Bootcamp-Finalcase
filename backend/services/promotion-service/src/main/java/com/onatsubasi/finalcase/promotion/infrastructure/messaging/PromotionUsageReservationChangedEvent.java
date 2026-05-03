package com.onatsubasi.finalcase.promotion.infrastructure.messaging;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservation;

import java.util.UUID;

public record PromotionUsageReservationChangedEvent(
        UUID reservationId,
        UUID orderId,
        UUID userId,
        PromotionUsageReservationStatus status
) {

    public static PromotionUsageReservationChangedEvent from(PromotionUsageReservation reservation) {
        return new PromotionUsageReservationChangedEvent(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getUserId(),
                reservation.getStatus()
        );
    }
}