package com.onatsubasi.finalcase.inventory.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.inventory.domain.enums.ReleaseReason;
import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservationItem;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record StockReservationPayload(
        UUID reservationId,
        String idempotencyKey,
        UUID checkoutId,
        UUID userId,
        UUID orderId,
        StockReservationStatus status,
        Instant reservedUntil,
        Instant confirmedAt,
        Instant releasedAt,
        ReleaseReason releaseReason,
        List<ItemPayload> items,
        Instant createdAt,
        Instant updatedAt
) {

    public record ItemPayload(
            UUID reservationItemId,
            UUID productId,
            int quantity,
            Instant createdAt
    ) {

        public static ItemPayload from(StockReservationItem item) {
            return new ItemPayload(
                    item.getId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getCreatedAt()
            );
        }
    }

    public static StockReservationPayload from(StockReservation reservation) {
        List<ItemPayload> items = reservation.getItems()
                .stream()
                .sorted(Comparator.comparing(StockReservationItem::getCreatedAt))
                .map(ItemPayload::from)
                .toList();

        return new StockReservationPayload(
                reservation.getId(),
                reservation.getIdempotencyKey(),
                reservation.getCheckoutId(),
                reservation.getUserId(),
                reservation.getOrderId(),
                reservation.getStatus(),
                reservation.getReservedUntil(),
                reservation.getConfirmedAt(),
                reservation.getReleasedAt(),
                reservation.getReleaseReason(),
                items,
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}