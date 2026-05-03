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
        name = "checkout_items",
        indexes = {
                @Index(name = "idx_checkout_items_checkout_id", columnList = "checkout_id"),
                @Index(name = "idx_checkout_items_product_id", columnList = "product_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckoutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_id", nullable = false)
    private CheckoutSession checkoutSession;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false, length = 120)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(length = 300)
    private String slug;

    @Column(name = "main_image_url", length = 1000)
    private String mainImageUrl;

    @Column(name = "brand_id")
    private UUID brandId;

    @Column(name = "brand_name", length = 150)
    private String brandName;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "category_name", length = 150)
    private String categoryName;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineSubtotal;

    @Column(name = "line_discount", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineDiscount;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private CheckoutItem(
            UUID productId,
            String sku,
            String productName,
            String slug,
            String mainImageUrl,
            UUID brandId,
            String brandName,
            UUID categoryId,
            String categoryName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineDiscount,
            String currency
    ) {
        validateUuid(productId, "Product id is required");
        validateRequired(sku, "SKU is required");
        validateRequired(productName, "Product name is required");
        validateMoney(unitPrice, "Unit price cannot be negative");
        validateQuantity(quantity);
        validateMoney(lineDiscount, "Line discount cannot be negative");
        validateRequired(currency, "Currency is required");

        this.productId = productId;
        this.sku = normalize(sku, 120);
        this.productName = normalize(productName, 255);
        this.slug = normalize(slug, 300);
        this.mainImageUrl = normalize(mainImageUrl, 1000);
        this.brandId = brandId;
        this.brandName = normalize(brandName, 150);
        this.categoryId = categoryId;
        this.categoryName = normalize(categoryName, 150);
        this.unitPrice = money(unitPrice);
        this.quantity = quantity;
        this.lineSubtotal = money(unitPrice).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        this.lineDiscount = money(lineDiscount);
        this.lineTotal = this.lineSubtotal.subtract(this.lineDiscount).setScale(2, RoundingMode.HALF_UP);

        if (this.lineTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_TOTALS, "Line total cannot be negative");
        }

        this.currency = currency.trim().toUpperCase(Locale.ROOT);
        this.createdAt = Instant.now();
    }

    public static CheckoutItem create(
            UUID productId,
            String sku,
            String productName,
            String slug,
            String mainImageUrl,
            UUID brandId,
            String brandName,
            UUID categoryId,
            String categoryName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineDiscount,
            String currency
    ) {
        return new CheckoutItem(
                productId,
                sku,
                productName,
                slug,
                mainImageUrl,
                brandId,
                brandName,
                categoryId,
                categoryName,
                unitPrice,
                quantity,
                lineDiscount,
                currency
        );
    }

    void assignTo(CheckoutSession checkoutSession) {
        if (checkoutSession == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Checkout item must belong to session");
        }

        this.checkoutSession = checkoutSession;
    }

    private void validateUuid(UUID value, String message) {
        if (value == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, message);
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, message);
        }
    }

    private void validateMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_TOTALS, message);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Quantity must be greater than zero");
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
