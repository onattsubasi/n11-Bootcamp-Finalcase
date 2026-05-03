package com.onatsubasi.finalcase.basket.domain.entity;

import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.basket.domain.exception.BasketErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "baskets",
        indexes = {
                @Index(name = "idx_baskets_user_id", columnList = "user_id"),
                @Index(name = "idx_baskets_status", columnList = "status"),
                @Index(name = "idx_baskets_order_id", columnList = "order_id"),
                @Index(name = "idx_baskets_updated_at", columnList = "updated_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Basket {

    private static final int MAX_COUPON_CODE_INTENT_LENGTH = 80;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BasketStatus status = BasketStatus.ACTIVE;

    /**
     * Customer-entered coupon code candidate.
     *
     * This is only checkout/quote UX intent. Basket does not validate coupon eligibility,
     * calculate discount, reserve coupon usage, or decide whether the coupon is applicable.
     */
    @Column(name = "coupon_code_intent", length = MAX_COUPON_CODE_INTENT_LENGTH)
    private String couponCodeIntent;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "checked_out_at")
    private Instant checkedOutAt;

    @Column(name = "cleared_at")
    private Instant clearedAt;

    @Column(name = "abandoned_at")
    private Instant abandonedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(
            mappedBy = "basket",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<BasketItem> items = new ArrayList<>();

    private Basket(UUID userId) {
        validateUserId(userId);

        this.id = UUID.randomUUID();
        this.userId = userId;
        this.status = BasketStatus.ACTIVE;
        this.items = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Basket empty(UUID userId) {
        return new Basket(userId);
    }

    public void addItem(UUID productId, int quantity) {
        addItem(productId, quantity, null, null, null, null);
    }

    public void addItem(
            UUID productId,
            int quantity,
            String productNameSnapshot,
            String imageUrlSnapshot,
            java.math.BigDecimal unitPriceSnapshot,
            String snapshotCurrency
    ) {
        ensureActive();

        Optional<BasketItem> existingItem = findItem(productId);

        if (existingItem.isPresent()) {
            BasketItem item = existingItem.get();
            item.increaseQuantity(quantity);
            item.updateSnapshot(
                    productNameSnapshot,
                    imageUrlSnapshot,
                    unitPriceSnapshot,
                    snapshotCurrency
            );
        } else {
            BasketItem item = new BasketItem(
                    this,
                    productId,
                    quantity,
                    productNameSnapshot,
                    imageUrlSnapshot,
                    unitPriceSnapshot,
                    snapshotCurrency
            );
            items.add(item);
        }

        touch();
    }

    public void updateItemQuantity(UUID productId, int quantity) {
        ensureActive();

        BasketItem item = findItem(productId)
                .orElseThrow(() -> new BaseException(BasketErrorCode.BASKET_ITEM_NOT_FOUND));

        item.changeQuantity(quantity);
        touch();
    }

    public void removeItem(UUID productId) {
        ensureActive();
        validateProductId(productId);

        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));

        if (removed) {
            touch();
        }
    }

    /**
     * Recommended customer clear behavior: remove items physically and keep basket ACTIVE.
     */
    public void clear() {
        ensureActive();

        boolean changed = !items.isEmpty() || couponCodeIntent != null;
        items.clear();
        couponCodeIntent = null;

        if (changed) {
            this.clearedAt = Instant.now();
            touch();
        }
    }

    /**
     * Optional lifecycle state, not used by the normal customer clear action.
     */
    public void markCleared() {
        ensureActive();
        this.items.clear();
        this.status = BasketStatus.CLEARED;
        this.clearedAt = Instant.now();
        touch();
    }

    public void setCouponCodeIntent(String couponCodeIntent) {
        ensureActive();

        this.couponCodeIntent = normalizeCouponCodeIntent(couponCodeIntent);
        touch();
    }

    public void clearCouponCodeIntent() {
        ensureActive();

        if (this.couponCodeIntent != null) {
            this.couponCodeIntent = null;
            touch();
        }
    }

    public void markCheckedOut(UUID orderId) {
        if (orderId == null) {
            throw new BaseException(BasketErrorCode.INVALID_ORDER_ID);
        }

        if (status == BasketStatus.CHECKED_OUT) {
            if (this.orderId != null && this.orderId.equals(orderId)) {
                return;
            }

            throw new BaseException(BasketErrorCode.BASKET_ALREADY_CHECKED_OUT);
        }

        ensureActive();

        if (isEmpty()) {
            throw new BaseException(BasketErrorCode.BASKET_EMPTY);
        }

        this.status = BasketStatus.CHECKED_OUT;
        this.orderId = orderId;
        this.checkedOutAt = Instant.now();
        touch();
    }

    public void markAbandoned() {
        if (status != BasketStatus.ACTIVE) {
            return;
        }

        this.status = BasketStatus.ABANDONED;
        this.abandonedAt = Instant.now();
        touch();
    }

    public void assertOwnedBy(UUID currentUserId) {
        validateUserId(currentUserId);

        if (!this.userId.equals(currentUserId)) {
            throw new BaseException(BasketErrorCode.BASKET_OWNERSHIP_VIOLATION);
        }
    }

    public boolean isActive() {
        return status == BasketStatus.ACTIVE;
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public int totalQuantity() {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        return items.stream()
                .mapToInt(BasketItem::getQuantity)
                .sum();
    }

    public int itemCount() {
        return items == null ? 0 : items.size();
    }

    public List<BasketItem> getItems() {
        return items == null ? List.of() : List.copyOf(items);
    }

    private Optional<BasketItem> findItem(UUID productId) {
        validateProductId(productId);

        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    private void ensureActive() {
        if (status != BasketStatus.ACTIVE) {
            throw new BaseException(BasketErrorCode.BASKET_NOT_ACTIVE);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new BaseException(BasketErrorCode.UNAUTHENTICATED_BASKET_ACCESS);
        }
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(BasketErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private String normalizeCouponCodeIntent(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        if (normalized.length() > MAX_COUPON_CODE_INTENT_LENGTH) {
            throw new BaseException(
                    BasketErrorCode.INVALID_COUPON_CODE_INTENT,
                    "Coupon code intent cannot exceed " + MAX_COUPON_CODE_INTENT_LENGTH + " characters"
            );
        }

        return normalized;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (status == null) {
            status = BasketStatus.ACTIVE;
        }

        if (items == null) {
            items = new ArrayList<>();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        couponCodeIntent = normalizeCouponCodeIntent(couponCodeIntent);
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
        couponCodeIntent = normalizeCouponCodeIntent(couponCodeIntent);
    }
}
