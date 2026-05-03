package com.onatsubasi.finalcase.promotion.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageCancelReason;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
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
        name = "promotion_usage_reservations",
        indexes = {
                @Index(name = "idx_promotion_usage_reservations_checkout_id", columnList = "checkout_id"),
                @Index(name = "idx_promotion_usage_reservations_order_id", columnList = "order_id"),
                @Index(name = "idx_promotion_usage_reservations_user_id", columnList = "user_id"),
                @Index(name = "idx_promotion_usage_reservations_status", columnList = "status"),
                @Index(name = "idx_promotion_usage_reservations_reserved_until", columnList = "reserved_until")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_promotion_usage_reservations_idempotency_key",
                        columnNames = "idempotency_key"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionUsageReservation {

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
    private PromotionUsageReservationStatus status = PromotionUsageReservationStatus.RESERVED;

    @Column(name = "reserved_until", nullable = false)
    private Instant reservedUntil;

    @Column(name = "redeemed_at")
    private Instant redeemedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_reason", length = 40)
    private PromotionUsageCancelReason cancelReason;

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
    private List<PromotionUsageReservationItem> items = new ArrayList<>();

    private PromotionUsageReservation(
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
        this.status = PromotionUsageReservationStatus.RESERVED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static PromotionUsageReservation create(
            String idempotencyKey,
            String requestHash,
            UUID checkoutId,
            UUID userId,
            Instant reservedUntil
    ) {
        return new PromotionUsageReservation(
                idempotencyKey,
                requestHash,
                checkoutId,
                userId,
                reservedUntil
        );
    }

    public void addItem(PromotionUsageReservationItem item) {
        ensureReserved();

        if (item == null) {
            throw new BaseException(PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION);
        }

        items.add(item);
        item.assignTo(this);
        touch();
    }

    public void assertNotEmpty() {
        if (items == null || items.isEmpty()) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION,
                    "Promotion usage reservation must contain at least one item"
            );
        }
    }

    public void assertSameRequestHash(String requestHash) {
        validateRequestHash(requestHash);

        if (!this.requestHash.equals(requestHash.trim())) {
            throw new BaseException(PromotionErrorCode.IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD);
        }
    }

    public boolean redeem(UUID orderId) {
        validateOrderId(orderId);

        if (status == PromotionUsageReservationStatus.REDEEMED) {
            if (this.orderId != null && this.orderId.equals(orderId)) {
                return false;
            }

            throw new BaseException(
                    PromotionErrorCode.PROMOTION_USAGE_RESERVATION_REDEEM_BLOCKED,
                    "Reservation was already redeemed with another order id"
            );
        }

        if (status == PromotionUsageReservationStatus.CANCELLED
                || status == PromotionUsageReservationStatus.EXPIRED) {
            throw new BaseException(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_REDEEM_BLOCKED);
        }

        ensureReserved();

        this.status = PromotionUsageReservationStatus.REDEEMED;
        this.orderId = orderId;
        this.redeemedAt = Instant.now();
        touch();

        return true;
    }

    public boolean cancel(PromotionUsageCancelReason reason) {
        if (status == PromotionUsageReservationStatus.CANCELLED
                || status == PromotionUsageReservationStatus.EXPIRED) {
            return false;
        }

        if (status == PromotionUsageReservationStatus.REDEEMED) {
            throw new BaseException(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_CANCEL_BLOCKED);
        }

        ensureReserved();

        this.status = PromotionUsageReservationStatus.CANCELLED;
        this.cancelReason = reason == null ? PromotionUsageCancelReason.UNKNOWN : reason;
        this.cancelledAt = Instant.now();
        touch();

        return true;
    }

    public boolean expire() {
        if (status == PromotionUsageReservationStatus.EXPIRED
                || status == PromotionUsageReservationStatus.CANCELLED) {
            return false;
        }

        if (status == PromotionUsageReservationStatus.REDEEMED) {
            throw new BaseException(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_CANCEL_BLOCKED);
        }

        ensureReserved();

        this.status = PromotionUsageReservationStatus.EXPIRED;
        this.cancelReason = PromotionUsageCancelReason.TIMEOUT;
        this.expiredAt = Instant.now();
        touch();

        return true;
    }

    public boolean isExpiredAt(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;
        return status == PromotionUsageReservationStatus.RESERVED
                && reservedUntil.isBefore(referenceTime);
    }

    public List<PromotionUsageReservationItem> getItems() {
        return items == null ? List.of() : List.copyOf(items);
    }

    private void ensureReserved() {
        if (status != PromotionUsageReservationStatus.RESERVED) {
            throw new BaseException(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_NOT_ACTIVE);
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BaseException(PromotionErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
    }

    private void validateRequestHash(String requestHash) {
        if (requestHash == null || requestHash.isBlank()) {
            throw new BaseException(PromotionErrorCode.INVALID_REQUEST_HASH);
        }
    }

    private void validateCheckoutId(UUID checkoutId) {
        if (checkoutId == null) {
            throw new BaseException(PromotionErrorCode.INVALID_CHECKOUT_ID);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new BaseException(PromotionErrorCode.INVALID_USER_ID);
        }
    }

    private void validateOrderId(UUID orderId) {
        if (orderId == null) {
            throw new BaseException(PromotionErrorCode.INVALID_ORDER_ID);
        }
    }

    private void validateReservedUntil(Instant reservedUntil) {
        if (reservedUntil == null || !reservedUntil.isAfter(Instant.now())) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION,
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
            status = PromotionUsageReservationStatus.RESERVED;
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
