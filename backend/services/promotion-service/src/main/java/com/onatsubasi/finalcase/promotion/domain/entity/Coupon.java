package com.onatsubasi.finalcase.promotion.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.enums.CouponStatus;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "coupons",
        indexes = {
                @Index(name = "idx_coupons_promotion_id", columnList = "promotion_id"),
                @Index(name = "idx_coupons_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_coupons_code", columnNames = "code")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponStatus status = CouponStatus.ACTIVE;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "per_user_usage_limit")
    private Integer perUserUsageLimit;

    @Column(name = "reserved_usage_count", nullable = false)
    private int reservedUsageCount;

    @Column(name = "redeemed_usage_count", nullable = false)
    private int redeemedUsageCount;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private Coupon(
            String code,
            Promotion promotion,
            Integer usageLimit,
            Integer perUserUsageLimit,
            Instant startsAt,
            Instant endsAt
    ) {
        validateCode(code);
        validatePromotion(promotion);
        validateLimit(usageLimit, "Coupon usage limit must be greater than zero");
        validateLimit(perUserUsageLimit, "Coupon per-user usage limit must be greater than zero");
        validateDateRange(startsAt, endsAt);

        this.code = normalizeCode(code);
        this.promotion = promotion;
        this.status = CouponStatus.ACTIVE;
        this.usageLimit = usageLimit;
        this.perUserUsageLimit = perUserUsageLimit;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Coupon create(
            String code,
            Promotion promotion,
            Integer usageLimit,
            Integer perUserUsageLimit,
            Instant startsAt,
            Instant endsAt
    ) {
        return new Coupon(
                code,
                promotion,
                usageLimit,
                perUserUsageLimit,
                startsAt,
                endsAt
        );
    }

    public void update(
            Integer usageLimit,
            Integer perUserUsageLimit,
            Instant startsAt,
            Instant endsAt
    ) {
        validateLimit(usageLimit, "Coupon usage limit must be greater than zero");
        validateLimit(perUserUsageLimit, "Coupon per-user usage limit must be greater than zero");
        validateDateRange(startsAt, endsAt);

        if (usageLimit != null && usedCount() > usageLimit) {
            throw new BaseException(
                    PromotionErrorCode.COUPON_USAGE_LIMIT_EXCEEDED,
                    "Coupon usage limit cannot be lower than current reserved + redeemed count"
            );
        }

        this.usageLimit = usageLimit;
        this.perUserUsageLimit = perUserUsageLimit;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        touch();
    }

    public void activate() {
        if (this.status == CouponStatus.ACTIVE) {
            return;
        }

        if (isExpiredAt(Instant.now())) {
            throw new BaseException(PromotionErrorCode.COUPON_EXPIRED);
        }

        this.status = CouponStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        if (this.status == CouponStatus.INACTIVE) {
            return;
        }

        this.status = CouponStatus.INACTIVE;
        touch();
    }

    public void expire() {
        if (this.status == CouponStatus.EXPIRED) {
            return;
        }

        this.status = CouponStatus.EXPIRED;
        touch();
    }

    public void validateApplicableAt(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;

        if (status != CouponStatus.ACTIVE) {
            throw new BaseException(PromotionErrorCode.COUPON_NOT_ACTIVE);
        }

        if (startsAt != null && startsAt.isAfter(referenceTime)) {
            throw new BaseException(PromotionErrorCode.COUPON_NOT_ACTIVE);
        }

        if (endsAt != null && endsAt.isBefore(referenceTime)) {
            throw new BaseException(PromotionErrorCode.COUPON_EXPIRED);
        }
    }

    public void reserveUsage() {
        validateApplicableAt(Instant.now());

        if (usageLimit != null && usedCount() + 1 > usageLimit) {
            throw new BaseException(PromotionErrorCode.COUPON_USAGE_LIMIT_EXCEEDED);
        }

        this.reservedUsageCount++;
        touch();
    }

    public void redeemReservedUsage() {
        if (reservedUsageCount <= 0) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION,
                    "Coupon has no reserved usage to redeem"
            );
        }

        this.reservedUsageCount--;
        this.redeemedUsageCount++;
        touch();
    }

    public void releaseReservedUsage() {
        if (reservedUsageCount <= 0) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION,
                    "Coupon has no reserved usage to release"
            );
        }

        this.reservedUsageCount--;
        touch();
    }

    public boolean isActiveAt(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;

        return status == CouponStatus.ACTIVE
                && (startsAt == null || !startsAt.isAfter(referenceTime))
                && (endsAt == null || !endsAt.isBefore(referenceTime));
    }

    public boolean isExpiredAt(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;
        return endsAt != null && endsAt.isBefore(referenceTime);
    }

    public int usedCount() {
        return reservedUsageCount + redeemedUsageCount;
    }

    public static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BaseException(PromotionErrorCode.INVALID_COUPON_DATA, "Coupon code is required");
        }

        String normalized = code.trim().toUpperCase(Locale.ROOT);

        if (normalized.length() > 80) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_COUPON_DATA,
                    "Coupon code cannot exceed 80 characters"
            );
        }

        return normalized;
    }

    private void validateCode(String code) {
        normalizeCode(code);
    }

    private void validatePromotion(Promotion promotion) {
        if (promotion == null) {
            throw new BaseException(PromotionErrorCode.INVALID_COUPON_DATA, "Promotion is required");
        }
    }

    private void validateLimit(Integer limit, String message) {
        if (limit != null && limit <= 0) {
            throw new BaseException(PromotionErrorCode.INVALID_COUPON_DATA, message);
        }
    }

    private void validateDateRange(Instant startsAt, Instant endsAt) {
        if (startsAt != null && endsAt != null && !endsAt.isAfter(startsAt)) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_COUPON_DATA,
                    "Coupon end date must be after start date"
            );
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        code = normalizeCode(code);

        if (status == null) {
            status = CouponStatus.ACTIVE;
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
        code = normalizeCode(code);
        updatedAt = Instant.now();
    }
}