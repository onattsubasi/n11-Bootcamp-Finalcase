package com.onatsubasi.finalcase.inventory.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.inventory.domain.enums.InventoryItemStatus;
import com.onatsubasi.finalcase.inventory.domain.enums.StockStatus;
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
        name = "inventory_items",
        indexes = {
                @Index(name = "idx_inventory_items_product_id", columnList = "product_id"),
                @Index(name = "idx_inventory_items_status", columnList = "status"),
                @Index(name = "idx_inventory_items_updated_at", columnList = "updated_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_items_product_id",
                        columnNames = "product_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryItemStatus status = InventoryItemStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private InventoryItem(UUID productId, int initialQuantity, int lowStockThreshold) {
        validateProductId(productId);
        validateNonNegativeQuantity(initialQuantity);
        validateLowStockThreshold(lowStockThreshold);

        this.productId = productId;
        this.totalQuantity = initialQuantity;
        this.reservedQuantity = 0;
        this.lowStockThreshold = lowStockThreshold;
        this.status = InventoryItemStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static InventoryItem create(UUID productId, int initialQuantity, int lowStockThreshold) {
        return new InventoryItem(productId, initialQuantity, lowStockThreshold);
    }

    public void increaseStock(int quantity) {
        ensureActive();
        validatePositiveQuantity(quantity);

        this.totalQuantity += quantity;
        touch();
    }

    public void decreaseStock(int quantity) {
        ensureActive();
        validatePositiveQuantity(quantity);

        int newTotalQuantity = this.totalQuantity - quantity;

        if (newTotalQuantity < this.reservedQuantity) {
            throw new BaseException(InventoryErrorCode.TOTAL_QUANTITY_BELOW_RESERVED);
        }

        this.totalQuantity = newTotalQuantity;
        touch();
    }

    public void setTotalQuantity(int totalQuantity) {
        ensureActive();
        validateNonNegativeQuantity(totalQuantity);

        if (totalQuantity < this.reservedQuantity) {
            throw new BaseException(InventoryErrorCode.TOTAL_QUANTITY_BELOW_RESERVED);
        }

        this.totalQuantity = totalQuantity;
        touch();
    }

    public void updateLowStockThreshold(int lowStockThreshold) {
        ensureActive();
        validateLowStockThreshold(lowStockThreshold);

        this.lowStockThreshold = lowStockThreshold;
        touch();
    }

    public void reserve(int quantity) {
        ensureActive();
        validatePositiveQuantity(quantity);

        if (availableQuantity() < quantity) {
            throw new BaseException(InventoryErrorCode.INSUFFICIENT_STOCK);
        }

        this.reservedQuantity += quantity;
        touch();
    }

    public void confirmSale(int quantity) {
        ensureActive();
        validatePositiveQuantity(quantity);

        if (this.reservedQuantity < quantity) {
            throw new BaseException(
                    InventoryErrorCode.INVALID_RESERVATION_STATUS,
                    "Reserved quantity is lower than confirmation quantity"
            );
        }

        if (this.totalQuantity < quantity) {
            throw new BaseException(
                    InventoryErrorCode.INVALID_RESERVATION_STATUS,
                    "Total quantity is lower than confirmation quantity"
            );
        }

        this.reservedQuantity -= quantity;
        this.totalQuantity -= quantity;
        touch();
    }

    public void releaseReserved(int quantity) {
        ensureActive();
        validatePositiveQuantity(quantity);

        if (this.reservedQuantity < quantity) {
            throw new BaseException(
                    InventoryErrorCode.INVALID_RESERVATION_STATUS,
                    "Reserved quantity is lower than release quantity"
            );
        }

        this.reservedQuantity -= quantity;
        touch();
    }

    public void deactivate() {
        ensureNotDeleted();

        this.status = InventoryItemStatus.INACTIVE;
        touch();
    }

    public void activate() {
        ensureNotDeleted();

        this.status = InventoryItemStatus.ACTIVE;
        touch();
    }

    public void softDelete() {
        if (this.reservedQuantity > 0) {
            throw new BaseException(
                    InventoryErrorCode.TOTAL_QUANTITY_BELOW_RESERVED,
                    "Inventory item with reserved stock cannot be deleted"
            );
        }

        this.status = InventoryItemStatus.DELETED;
        touch();
    }

    public int availableQuantity() {
        return this.totalQuantity - this.reservedQuantity;
    }

    public StockStatus stockStatus() {
        if (availableQuantity() <= 0) {
            return StockStatus.OUT_OF_STOCK;
        }

        if (availableQuantity() <= this.lowStockThreshold) {
            return StockStatus.LOW_STOCK;
        }

        return StockStatus.IN_STOCK;
    }

    public boolean isActive() {
        return this.status == InventoryItemStatus.ACTIVE;
    }

    private void ensureActive() {
        if (this.status != InventoryItemStatus.ACTIVE) {
            throw new BaseException(InventoryErrorCode.INVENTORY_ITEM_NOT_ACTIVE);
        }
    }

    private void ensureNotDeleted() {
        if (this.status == InventoryItemStatus.DELETED) {
            throw new BaseException(InventoryErrorCode.INVENTORY_ITEM_NOT_ACTIVE);
        }
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(InventoryErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BaseException(InventoryErrorCode.INVALID_POSITIVE_QUANTITY);
        }
    }

    private void validateNonNegativeQuantity(int quantity) {
        if (quantity < 0) {
            throw new BaseException(InventoryErrorCode.INVALID_QUANTITY);
        }
    }

    private void validateLowStockThreshold(int lowStockThreshold) {
        if (lowStockThreshold < 0) {
            throw new BaseException(InventoryErrorCode.INVALID_LOW_STOCK_THRESHOLD);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (status == null) {
            status = InventoryItemStatus.ACTIVE;
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}