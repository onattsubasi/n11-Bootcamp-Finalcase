package com.onatsubasi.finalcase.inventory.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.inventory.domain.enums.StockMovementType;
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
        name = "stock_movements",
        indexes = {
                @Index(name = "idx_stock_movements_product_id", columnList = "product_id"),
                @Index(name = "idx_stock_movements_inventory_item_id", columnList = "inventory_item_id"),
                @Index(name = "idx_stock_movements_reservation_id", columnList = "reservation_id"),
                @Index(name = "idx_stock_movements_type", columnList = "movement_type"),
                @Index(name = "idx_stock_movements_occurred_at", columnList = "occurred_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "inventory_item_id", nullable = false, updatable = false)
    private UUID inventoryItemId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 40)
    private StockMovementType movementType;

    @Column(name = "quantity_change", nullable = false, updatable = false)
    private int quantityChange;

    @Column(name = "total_before", nullable = false, updatable = false)
    private int totalBefore;

    @Column(name = "reserved_before", nullable = false, updatable = false)
    private int reservedBefore;

    @Column(name = "total_after", nullable = false, updatable = false)
    private int totalAfter;

    @Column(name = "reserved_after", nullable = false, updatable = false)
    private int reservedAfter;

    @Column(name = "reservation_id", updatable = false)
    private UUID reservationId;

    @Column(name = "checkout_id", updatable = false)
    private UUID checkoutId;

    @Column(name = "order_id", updatable = false)
    private UUID orderId;

    @Column(length = 500)
    private String reason;

    @Column(name = "reference_id", length = 120)
    private String referenceId;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    private StockMovement(
            UUID inventoryItemId,
            UUID productId,
            StockMovementType movementType,
            int quantityChange,
            int totalBefore,
            int reservedBefore,
            int totalAfter,
            int reservedAfter,
            UUID reservationId,
            UUID checkoutId,
            UUID orderId,
            String reason,
            String referenceId
    ) {
        validateInventoryItemId(inventoryItemId);
        validateProductId(productId);
        validateMovementType(movementType);

        this.inventoryItemId = inventoryItemId;
        this.productId = productId;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.totalBefore = totalBefore;
        this.reservedBefore = reservedBefore;
        this.totalAfter = totalAfter;
        this.reservedAfter = reservedAfter;
        this.reservationId = reservationId;
        this.checkoutId = checkoutId;
        this.orderId = orderId;
        this.reason = normalize(reason, 500);
        this.referenceId = normalize(referenceId, 120);
        this.occurredAt = Instant.now();
    }

    public static StockMovement create(
            UUID inventoryItemId,
            UUID productId,
            StockMovementType movementType,
            int quantityChange,
            int totalBefore,
            int reservedBefore,
            int totalAfter,
            int reservedAfter,
            UUID reservationId,
            UUID checkoutId,
            UUID orderId,
            String reason,
            String referenceId
    ) {
        return new StockMovement(
                inventoryItemId,
                productId,
                movementType,
                quantityChange,
                totalBefore,
                reservedBefore,
                totalAfter,
                reservedAfter,
                reservationId,
                checkoutId,
                orderId,
                reason,
                referenceId
        );
    }

    private void validateInventoryItemId(UUID inventoryItemId) {
        if (inventoryItemId == null) {
            throw new BaseException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND);
        }
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(InventoryErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private void validateMovementType(StockMovementType movementType) {
        if (movementType == null) {
            throw new BaseException(
                    InventoryErrorCode.INVALID_RESERVATION_STATUS,
                    "Stock movement type is required"
            );
        }
    }

    private String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        return normalized.length() > maxLength
                ? normalized.substring(0, maxLength)
                : normalized;
    }

    @PrePersist
    protected void prePersist() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}