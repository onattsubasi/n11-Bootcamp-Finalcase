package com.onatsubasi.finalcase.basket.domain.entity;

import com.onatsubasi.finalcase.basket.domain.enums.BasketItemStatus;
import com.onatsubasi.finalcase.basket.domain.exception.BasketErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "basket_items",
        indexes = {
                @Index(name = "idx_basket_items_basket_id", columnList = "basket_id"),
                @Index(name = "idx_basket_items_product_id", columnList = "product_id"),
                @Index(name = "idx_basket_items_status", columnList = "item_status")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_basket_items_basket_product",
                        columnNames = {"basket_id", "product_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BasketItem {

    public static final int MAX_QUANTITY = 99;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", nullable = false, updatable = false)
    private Basket basket;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 30)
    private BasketItemStatus itemStatus = BasketItemStatus.ACTIVE;

    /**
     * Optional UX-only snapshot.
     * This is not authoritative and must not be trusted during checkout.
     */
    @Column(name = "product_name_snapshot", length = 255)
    private String productNameSnapshot;

    /**
     * Optional UX-only snapshot.
     */
    @Column(name = "image_url_snapshot", length = 1000)
    private String imageUrlSnapshot;

    /**
     * Optional UX-only snapshot.
     * Do not use this for final checkout pricing.
     */
    @Column(name = "unit_price_snapshot", precision = 19, scale = 2)
    private BigDecimal unitPriceSnapshot;

    /**
     * Optional UX-only snapshot.
     */
    @Column(name = "snapshot_currency", length = 3)
    private String snapshotCurrency;

    @Column(name = "stale_reason", length = 255)
    private String staleReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public BasketItem(
            Basket basket,
            UUID productId,
            int quantity
    ) {
        this(
                basket,
                productId,
                quantity,
                null,
                null,
                null,
                null
        );
    }

    public BasketItem(
            Basket basket,
            UUID productId,
            int quantity,
            String productNameSnapshot,
            String imageUrlSnapshot,
            BigDecimal unitPriceSnapshot,
            String snapshotCurrency
    ) {
        if (basket == null) {
            throw new BaseException(BasketErrorCode.BASKET_NOT_FOUND);
        }

        validateProductId(productId);
        validateQuantity(quantity);

        this.basket = basket;
        this.productId = productId;
        this.quantity = quantity;
        this.itemStatus = BasketItemStatus.ACTIVE;
        updateSnapshot(
                productNameSnapshot,
                imageUrlSnapshot,
                unitPriceSnapshot,
                snapshotCurrency
        );
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void increaseQuantity(int amount) {
        validateQuantity(amount);

        int newQuantity = this.quantity + amount;

        if (newQuantity > MAX_QUANTITY) {
            throw new BaseException(BasketErrorCode.BASKET_ITEM_QUANTITY_LIMIT_EXCEEDED);
        }

        this.quantity = newQuantity;
        touch();
    }

    public void changeQuantity(int quantity) {
        validateQuantity(quantity);

        this.quantity = quantity;
        touch();
    }

    public void updateSnapshot(
            String productNameSnapshot,
            String imageUrlSnapshot,
            BigDecimal unitPriceSnapshot,
            String snapshotCurrency
    ) {
        this.productNameSnapshot = normalize(productNameSnapshot, 255);
        this.imageUrlSnapshot = normalize(imageUrlSnapshot, 1000);
        this.unitPriceSnapshot = normalizePrice(unitPriceSnapshot);
        this.snapshotCurrency = normalizeCurrency(snapshotCurrency);
        touch();
    }

    public void markActive() {
        this.itemStatus = BasketItemStatus.ACTIVE;
        this.staleReason = null;
        touch();
    }

    public void markStale(String reason) {
        this.itemStatus = BasketItemStatus.STALE;
        this.staleReason = normalize(reason, 255);
        touch();
    }

    public void markUnavailable(String reason) {
        this.itemStatus = BasketItemStatus.UNAVAILABLE;
        this.staleReason = normalize(reason, 255);
        touch();
    }

    public void markPriceChanged(String reason) {
        this.itemStatus = BasketItemStatus.PRICE_CHANGED;
        this.staleReason = normalize(reason, 255);
        touch();
    }

    public void markRemovedBySystem(String reason) {
        this.itemStatus = BasketItemStatus.REMOVED_BY_SYSTEM;
        this.staleReason = normalize(reason, 255);
        touch();
    }

    /**
     * Backward-compatible convenience method.
     */
    public void markStale() {
        markStale("Product information may have changed");
    }

    /**
     * Backward-compatible convenience method.
     */
    public void markUnavailable() {
        markUnavailable("Product is no longer available");
    }

    /**
     * Backward-compatible convenience method.
     */
    public void markPriceChanged() {
        markPriceChanged("Product price may have changed");
    }

    /**
     * Backward-compatible convenience method.
     */
    public void markRemovedBySystem() {
        markRemovedBySystem("Product was removed by system");
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(BasketErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new BaseException(BasketErrorCode.INVALID_QUANTITY);
        }

        if (quantity > MAX_QUANTITY) {
            throw new BaseException(BasketErrorCode.BASKET_ITEM_QUANTITY_LIMIT_EXCEEDED);
        }
    }

    private BigDecimal normalizePrice(BigDecimal value) {
        if (value == null) {
            return null;
        }

        if (value.signum() <= 0) {
            throw new BaseException(
                    BasketErrorCode.BASKET_INVALID_QUANTITY,
                    "Snapshot unit price must be greater than zero when provided"
            );
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCurrency(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        if (normalized.length() != 3) {
            throw new BaseException(
                    BasketErrorCode.INVALID_COUPON_CODE_INTENT,
                    "Snapshot currency must be a 3-letter ISO code"
            );
        }

        return normalized;
    }

    private String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            return normalized.substring(0, maxLength);
        }

        return normalized;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (itemStatus == null) {
            itemStatus = BasketItemStatus.ACTIVE;
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