package com.onatsubasi.finalcase.checkout.domain.entity;

import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
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
        name = "checkout_discounts",
        indexes = {
                @Index(name = "idx_checkout_discounts_checkout_id", columnList = "checkout_id"),
                @Index(name = "idx_checkout_discounts_promotion_id", columnList = "promotion_id"),
                @Index(name = "idx_checkout_discounts_coupon_id", columnList = "coupon_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckoutDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_id", nullable = false)
    private CheckoutSession checkoutSession;

    @Column(name = "promotion_id")
    private UUID promotionId;

    @Column(name = "coupon_id")
    private UUID couponId;

    @Column(name = "coupon_code", length = 100)
    private String couponCode;

    @Column(name = "promotion_name", length = 150)
    private String promotionName;

    @Column(name = "promotion_type", length = 50)
    private String promotionType;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private CheckoutDiscount(
            UUID promotionId,
            UUID couponId,
            String couponCode,
            String promotionName,
            String promotionType,
            BigDecimal discountAmount,
            String currency
    ) {
        validateMoney(discountAmount, "Discount amount cannot be negative");
        validateRequired(currency, "Currency is required");

        this.promotionId = promotionId;
        this.couponId = couponId;
        this.couponCode = normalize(couponCode, 100);
        this.promotionName = normalize(promotionName, 150);
        this.promotionType = normalize(promotionType, 50);
        this.discountAmount = money(discountAmount);
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
        this.createdAt = Instant.now();
    }

    public static CheckoutDiscount create(
            UUID promotionId,
            UUID couponId,
            String couponCode,
            String promotionName,
            String promotionType,
            BigDecimal discountAmount,
            String currency
    ) {
        return new CheckoutDiscount(
                promotionId,
                couponId,
                couponCode,
                promotionName,
                promotionType,
                discountAmount,
                currency
        );
    }

    void assignTo(CheckoutSession checkoutSession) {
        if (checkoutSession == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Checkout discount must belong to session");
        }

        this.checkoutSession = checkoutSession;
    }

    private void validateMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_TOTALS, message);
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, message);
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
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
