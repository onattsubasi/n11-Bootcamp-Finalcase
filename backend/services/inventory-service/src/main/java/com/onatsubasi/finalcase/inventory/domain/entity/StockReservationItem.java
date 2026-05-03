package com.onatsubasi.finalcase.inventory.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.inventory.domain.exception.InventoryErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "stock_reservation_items",
        indexes = {
                @Index(name = "idx_stock_reservation_items_reservation_id", columnList = "reservation_id"),
                @Index(name = "idx_stock_reservation_items_product_id", columnList = "product_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, updatable = false)
    private StockReservation reservation;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public StockReservationItem(
            StockReservation reservation,
            UUID productId,
            int quantity
    ) {
        validateReservation(reservation);
        validateProductId(productId);
        validateQuantity(quantity);

        this.reservation = reservation;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = Instant.now();
    }

    private void validateReservation(StockReservation reservation) {
        if (reservation == null) {
            throw new BaseException(InventoryErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(InventoryErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BaseException(InventoryErrorCode.INVALID_POSITIVE_QUANTITY);
        }
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}