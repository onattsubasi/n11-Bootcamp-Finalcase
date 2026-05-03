package com.onatsubasi.finalcase.promotion.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.enums.CouponAssignmentStatus;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "coupon_assignments",
        indexes = {
                @Index(name = "idx_coupon_assignments_user_id", columnList = "user_id"),
                @Index(name = "idx_coupon_assignments_coupon_id", columnList = "coupon_id"),
                @Index(name = "idx_coupon_assignments_status", columnList = "status"),
                @Index(name = "idx_coupon_assignments_expires_at", columnList = "expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coupon_assignments_coupon_user",
                        columnNames = {"coupon_id", "user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponAssignmentStatus status = CouponAssignmentStatus.ASSIGNED;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "reserved_at")
    private Instant reservedAt;

    @Column(name = "redeemed_at")
    private Instant redeemedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private CouponAssignment(
            Coupon coupon,
            UUID userId,
            Instant expiresAt
    ) {
        validateCoupon(coupon);
        validateUserId(userId);

        this.coupon = coupon;
        this.userId = userId;
        this.status = CouponAssignmentStatus.ASSIGNED;
        this.assignedAt = Instant.now();
        this.expiresAt = expiresAt;
        this.updatedAt = this.assignedAt;
    }

    public static CouponAssignment assign(
            Coupon coupon,
            UUID userId,
            Instant expiresAt
    ) {
        return new CouponAssignment(coupon, userId, expiresAt);
    }

    public void validateUsableAt(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;

        if (status != CouponAssignmentStatus.ASSIGNED) {
            throw new BaseException(PromotionErrorCode.COUPON_ASSIGNMENT_NOT_ACTIVE);
        }

        if (expiresAt != null && expiresAt.isBefore(referenceTime)) {
            throw new BaseException(PromotionErrorCode.COUPON_EXPIRED);
        }
    }

    public boolean reserve() {
        if (status == CouponAssignmentStatus.RESERVED) {
            return false;
        }

        validateUsableAt(Instant.now());

        this.status = CouponAssignmentStatus.RESERVED;
        this.reservedAt = Instant.now();
        this.cancelledAt = null;
        touch();

        return true;
    }

    public boolean cancelReservation() {
        if (status == CouponAssignmentStatus.ASSIGNED) {
            return false;
        }

        if (status == CouponAssignmentStatus.RESERVED) {
            this.status = CouponAssignmentStatus.ASSIGNED;
            this.reservedAt = null;
            this.cancelledAt = null;
            touch();
            return true;
        }

        if (status == CouponAssignmentStatus.REDEEMED) {
            throw new BaseException(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_CANCEL_BLOCKED);
        }

        return false;
    }

    public boolean redeem() {
        if (status == CouponAssignmentStatus.REDEEMED) {
            return false;
        }

        if (status != CouponAssignmentStatus.RESERVED) {
            throw new BaseException(PromotionErrorCode.COUPON_ASSIGNMENT_NOT_ACTIVE);
        }

        this.status = CouponAssignmentStatus.REDEEMED;
        this.redeemedAt = Instant.now();
        touch();

        return true;
    }

    public boolean expire() {
        if (status == CouponAssignmentStatus.EXPIRED) {
            return false;
        }

        if (status == CouponAssignmentStatus.REDEEMED) {
            return false;
        }

        this.status = CouponAssignmentStatus.EXPIRED;
        this.expiredAt = Instant.now();
        touch();

        return true;
    }

    public boolean cancel() {
        if (status == CouponAssignmentStatus.CANCELLED) {
            return false;
        }

        if (status == CouponAssignmentStatus.REDEEMED) {
            throw new BaseException(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_CANCEL_BLOCKED);
        }

        this.status = CouponAssignmentStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        touch();

        return true;
    }

    public boolean isExpiredAt(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;
        return expiresAt != null && expiresAt.isBefore(referenceTime);
    }

    private void validateCoupon(Coupon coupon) {
        if (coupon == null) {
            throw new BaseException(PromotionErrorCode.INVALID_COUPON_ASSIGNMENT_DATA);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new BaseException(PromotionErrorCode.INVALID_USER_ID);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (status == null) {
            status = CouponAssignmentStatus.ASSIGNED;
        }

        if (assignedAt == null) {
            assignedAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = assignedAt;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}