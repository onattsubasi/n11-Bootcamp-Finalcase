package com.onatsubasi.finalcase.order.domain.entity;

import jakarta.persistence.*;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.domain.exception.OrderErrorCode;
import lombok.Getter;

import java.util.UUID;
import java.math.BigDecimal;
import java.util.Locale;


@Entity
@Table(
        name = "order_discounts",
        indexes = {
                @Index(name = "idx_order_discounts_order_id", columnList = "order_id"),
                @Index(name = "idx_order_discounts_promotion_id", columnList = "promotion_id"),
                @Index(name = "idx_order_discounts_coupon_code", columnList = "coupon_code")
        }
)
public class OrderDiscount {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Getter
    @Column(name = "promotion_id")
    private UUID promotionId;

    @Getter
    @Column(name = "promotion_name", length = 200)
    private String promotionName;

    @Getter
    @Column(name = "coupon_id")
    private UUID couponId;

    @Getter
    @Column(name = "coupon_code", length = 80)
    private String couponCode;

    @Getter
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Getter
    @Column(name = "shipping_discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingDiscountAmount;

    protected OrderDiscount() {
    }

    public OrderDiscount(
            UUID promotionId,
            String promotionName,
            UUID couponId,
            String couponCode,
            BigDecimal discountAmount,
            BigDecimal shippingDiscountAmount
    ) {
        validateMoney(discountAmount, "Discount amount cannot be negative");
        validateMoney(shippingDiscountAmount, "Shipping discount amount cannot be negative");

        this.promotionId = promotionId;
        this.promotionName = normalize(promotionName);
        this.couponId = couponId;
        this.couponCode = normalizeCode(couponCode);
        this.discountAmount = discountAmount;
        this.shippingDiscountAmount = shippingDiscountAmount;
    }

    void assignTo(Order order) {
        if (order == null) {
            throw new BaseException(
                    OrderErrorCode.INVALID_ORDER_DISCOUNT,
                    "Order discount must belong to an order"
            );
        }

        this.order = order;
    }

    private void validateMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_DISCOUNT, message);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private String normalizeCode(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }
}