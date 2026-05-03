package com.onatsubasi.finalcase.inventory.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.inventory.domain.enums.ReleaseReason;
import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import com.onatsubasi.finalcase.inventory.domain.exception.InventoryErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "stock_reservations",
        indexes = {
                @Index(name = "idx_stock_reservations_user_id", columnList = "user_id"),
                @Index(name = "idx_stock_reservations_checkout_id", columnList = "checkout_id"),
                @Index(name = "idx_stock_reservations_order_id", columnList = "order_id"),
                @Index(name = "idx_stock_reservations_status", columnList = "status"),
                @Index(name = "idx_stock_reservations_reserved_until", columnList = "reserved_until")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_reservations_idempotency_key",
                        columnNames = "idempotency_key"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 120)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, updatable = false, length = 128)
    private String requestHash;

    @Column(name = "checkout_id", nullable = false, updatable = false)
    private UUID checkoutId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "order_id")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockReservationStatus status = StockReservationStatus.RESERVED;

    @Column(name = "reserved_until", nullable = false)
    private Instant reservedUntil;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_reason", length = 40)
    private ReleaseReason releaseReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<StockReservationItem> items = new ArrayList<>();

    private StockReservation(
            String idempotencyKey,
            String requestHash,
            UUID checkoutId,
            UUID userId,
            Instant reservedUntil
    ) {
        validateIdempotencyKey(idempotencyKey);
        validateRequestHash(requestHash);
        validateCheckoutId(checkoutId);
        validateUserId(userId);
        validateReservedUntil(reservedUntil);

        this.idempotencyKey = idempotencyKey.trim();
        this.requestHash = requestHash.trim();
        this.checkoutId = checkoutId;
        this.userId = userId;
        this.reservedUntil = reservedUntil;
        this.status = StockReservationStatus.RESERVED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static StockReservation create(
            String idempotencyKey,
            String requestHash,
            UUID checkoutId,
            UUID userId,
            Instant reservedUntil
    ) {
        return new StockReservation(
                idempotencyKey,
                requestHash,
                checkoutId,
                userId,
                reservedUntil
        );
    }

    public void addItem(UUID productId, int quantity) {
        ensureReserved();
        this.items.add(new StockReservationItem(this, productId, quantity));
        touch();
    }

    public void assertNotEmpty() {
        if (items == null || items.isEmpty()) {
            throw new BaseException(InventoryErrorCode.RESERVATION_EMPTY);
        }
    }

    public void assertSameRequestHash(String requestHash) {
        validateRequestHash(requestHash);

        if (!this.requestHash.equals(requestHash.trim())) {
            throw new BaseException(InventoryErrorCode.IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD);
        }
    }

    public void confirm(UUID orderId) {
        validateOrderId(orderId);

        if (status == StockReservationStatus.CONFIRMED) {
            if (this.orderId != null && this.orderId.equals(orderId)) {
                return;
            }

            throw new BaseException(InventoryErrorCode.RESERVATION_ALREADY_CONFIRMED);
        }

        if (status == StockReservationStatus.RELEASED || status == StockReservationStatus.EXPIRED) {
            throw new BaseException(InventoryErrorCode.RESERVATION_CONFIRM_BLOCKED);
        }

        ensureReserved();

        this.status = StockReservationStatus.CONFIRMED;
        this.orderId = orderId;
        this.confirmedAt = Instant.now();
        touch();
    }

    public boolean release(ReleaseReason reason) {
        if (status == StockReservationStatus.RELEASED || status == StockReservationStatus.EXPIRED) {
            return false;
        }

        if (status == StockReservationStatus.CONFIRMED) {
            throw new BaseException(InventoryErrorCode.RESERVATION_RELEASE_BLOCKED);
        }

        ensureReserved();

        this.status = StockReservationStatus.RELEASED;
        this.releaseReason = reason == null ? ReleaseReason.UNKNOWN : reason;
        this.releasedAt = Instant.now();
        touch();

        return true;
    }

    public boolean expire() {
        if (status == StockReservationStatus.EXPIRED || status == StockReservationStatus.RELEASED) {
            return false;
        }

        if (status == StockReservationStatus.CONFIRMED) {
            throw new BaseException(InventoryErrorCode.RESERVATION_RELEASE_BLOCKED);
        }

        ensureReserved();

        this.status = StockReservationStatus.EXPIRED;
        this.releaseReason = ReleaseReason.TIMEOUT;
        this.releasedAt = Instant.now();
        touch();

        return true;
    }

    public boolean isReserved() {
        return status == StockReservationStatus.RESERVED;
    }

    public boolean isExpired(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;
        return reservedUntil.isBefore(referenceTime);
    }

    public List<StockReservationItem> getItems() {
        return items == null ? List.of() : List.copyOf(items);
    }

    private void ensureReserved() {
        if (status != StockReservationStatus.RESERVED) {
            throw new BaseException(InventoryErrorCode.INVALID_RESERVATION_STATUS);
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BaseException(InventoryErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
    }

    private void validateRequestHash(String requestHash) {
        if (requestHash == null || requestHash.isBlank()) {
            throw new BaseException(InventoryErrorCode.INVALID_REQUEST_HASH);
        }
    }

    private void validateCheckoutId(UUID checkoutId) {
        if (checkoutId == null) {
            throw new BaseException(InventoryErrorCode.INVALID_CHECKOUT_ID);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new BaseException(InventoryErrorCode.INVALID_USER_ID);
        }
    }

    private void validateOrderId(UUID orderId) {
        if (orderId == null) {
            throw new BaseException(InventoryErrorCode.INVALID_ORDER_ID);
        }
    }

    private void validateReservedUntil(Instant reservedUntil) {
        if (reservedUntil == null || !reservedUntil.isAfter(Instant.now())) {
            throw new BaseException(
                    InventoryErrorCode.RESERVATION_EXPIRED,
                    "reservedUntil must be in the future"
            );
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (status == null) {
            status = StockReservationStatus.RESERVED;
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
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}