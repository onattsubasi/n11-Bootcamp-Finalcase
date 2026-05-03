package com.onatsubasi.finalcase.promotion.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
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
        name = "promotion_usage_reservation_items",
        indexes = {
                @Index(name = "idx_promotion_usage_reservation_items_reservation_id", columnList = "reservation_id"),
                @Index(name = "idx_promotion_usage_reservation_items_promotion_id", columnList = "promotion_id"),
                @Index(name = "idx_promotion_usage_reservation_items_coupon_id", columnList = "coupon_id"),
                @Index(name = "idx_promotion_usage_reservation_items_assignment_id", columnList = "coupon_assignment_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionUsageReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private PromotionUsageReservation reservation;

    @Column(name = "promotion_id", nullable = false, updatable = false)
    private UUID promotionId;

    @Column(name = "coupon_id", updatable = false)
    private UUID couponId;

    @Column(name = "coupon_assignment_id", updatable = false)
    private UUID couponAssignmentId;

    @Column(name = "coupon_code", length = 80, updatable = false)
    private String couponCode;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal discountAmount;

    @Column(name = "shipping_discount_amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal shippingDiscountAmount;

    @Column(length = 300, updatable = false)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private PromotionUsageReservationItem(
            UUID promotionId,
            UUID couponId,
            UUID couponAssignmentId,
            String couponCode,
            BigDecimal discountAmount,
            BigDecimal shippingDiscountAmount,
            String description
    ) {
        validatePromotionId(promotionId);

        this.promotionId = promotionId;
        this.couponId = couponId;
        this.couponAssignmentId = couponAssignmentId;
        this.couponCode = normalizeCode(couponCode);
        this.discountAmount = normalizeAmount(discountAmount, "Discount amount cannot be negative");
        this.shippingDiscountAmount = normalizeAmount(
                shippingDiscountAmount,
                "Shipping discount amount cannot be negative"
        );
        this.description = normalize(description, 300);
        this.createdAt = Instant.now();
    }

    public static PromotionUsageReservationItem create(
            UUID promotionId,
            UUID couponId,
            UUID couponAssignmentId,
            String couponCode,
            BigDecimal discountAmount,
            BigDecimal shippingDiscountAmount,
            String description
    ) {
        return new PromotionUsageReservationItem(
                promotionId,
                couponId,
                couponAssignmentId,
                couponCode,
                discountAmount,
                shippingDiscountAmount,
                description
        );
    }

    void assignTo(PromotionUsageReservation reservation) {
        if (reservation == null) {
            throw new BaseException(PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION);
        }

        this.reservation = reservation;
    }

    public BigDecimal totalDiscountAmount() {
        return discountAmount.add(shippingDiscountAmount);
    }

    private void validatePromotionId(UUID promotionId) {
        if (promotionId == null) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION,
                    "Promotion id is required"
            );
        }
    }

    private BigDecimal normalizeAmount(BigDecimal value, String message) {
        BigDecimal normalized = value == null ? BigDecimal.ZERO : value;

        if (normalized.signum() < 0) {
            throw new BaseException(PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION, message);
        }

        return normalized.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String code) {
        return code == null || code.isBlank()
                ? null
                : code.trim().toUpperCase(Locale.ROOT);
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
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}