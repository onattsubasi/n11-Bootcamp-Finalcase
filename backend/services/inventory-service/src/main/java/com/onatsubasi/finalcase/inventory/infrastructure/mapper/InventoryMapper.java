package com.onatsubasi.finalcase.inventory.infrastructure.mapper;

import com.onatsubasi.finalcase.inventory.application.dto.response.*;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.entity.StockMovement;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservationItem;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class InventoryMapper {

    public InventoryItemResponse toResponse(InventoryItem item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getProductId(),
                item.getTotalQuantity(),
                item.getReservedQuantity(),
                item.availableQuantity(),
                item.getLowStockThreshold(),
                item.getStatus(),
                item.stockStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    public StockReservationResponse toResponse(StockReservation reservation) {
        List<StockReservationItemResponse> items = reservation.getItems()
                .stream()
                .sorted(Comparator.comparing(StockReservationItem::getCreatedAt))
                .map(this::toResponse)
                .toList();

        return new StockReservationResponse(
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

    public StockReservationItemResponse toResponse(StockReservationItem item) {
        return new StockReservationItemResponse(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getCreatedAt()
        );
    }

    public ReservationStatusResponse toStatusResponse(StockReservation reservation) {
        return new ReservationStatusResponse(
                reservation.getId(),
                reservation.getCheckoutId(),
                reservation.getOrderId(),
                reservation.getStatus(),
                reservation.getReservedUntil(),
                reservation.getConfirmedAt(),
                reservation.getReleasedAt(),
                reservation.getReleaseReason()
        );
    }

    public StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getInventoryItemId(),
                movement.getProductId(),
                movement.getMovementType(),
                movement.getQuantityChange(),
                movement.getTotalBefore(),
                movement.getReservedBefore(),
                movement.getTotalAfter(),
                movement.getReservedAfter(),
                movement.getReservationId(),
                movement.getCheckoutId(),
                movement.getOrderId(),
                movement.getReason(),
                movement.getReferenceId(),
                movement.getOccurredAt()
        );
    }
}