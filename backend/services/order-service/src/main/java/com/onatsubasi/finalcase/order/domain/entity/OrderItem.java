package com.onatsubasi.finalcase.order.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.domain.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order_id", columnList = "order_id"),
                @Index(name = "idx_order_items_product_id", columnList = "product_id")
        }
)
public class OrderItem {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Getter
    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Getter
    @Column(nullable = false, length = 120)
    private String sku;

    @Getter
    @Column(name = "product_name", nullable = false, length = 250)
    private String productName;

    @Getter
    @Column(nullable = false, length = 180)
    private String slug;

    @Getter
    @Column(name = "main_image_url", length = 1000)
    private String mainImageUrl;

    @Getter
    @Column(name = "brand_id", length = 100)
    private String brandId;

    @Getter
    @Column(name = "brand_name", length = 150)
    private String brandName;

    @Getter
    @Column(name = "category_id", length = 100)
    private String categoryId;

    @Getter
    @Column(name = "category_name", length = 150)
    private String categoryName;

    @Getter
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Getter
    @Column(nullable = false)
    private int quantity;

    @Getter
    @Column(name = "line_subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineSubtotal;

    @Getter
    @Column(name = "line_discount", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineDiscount;

    @Getter
    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    @Getter
    @Column(nullable = false, length = 3)
    private String currency;

    protected OrderItem() {
    }

    public OrderItem(String productId, String sku, String productName, String slug, String mainImageUrl, String brandId, String brandName, String categoryId, String categoryName, BigDecimal unitPrice, int quantity, BigDecimal lineSubtotal, BigDecimal lineDiscount, BigDecimal lineTotal, String currency) {
        validateRequired(productId, "Product id is required");
        validateRequired(sku, "SKU is required");
        validateRequired(productName, "Product name is required");
        validateRequired(slug, "Slug is required");
        validateMoney(unitPrice, "Unit price cannot be negative");
        validateMoney(lineSubtotal, "Line subtotal cannot be negative");
        validateMoney(lineDiscount, "Line discount cannot be negative");
        validateMoney(lineTotal, "Line total cannot be negative");
        validateRequired(currency, "Currency is required");

        if (quantity <= 0) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ITEM_DATA, "Quantity must be greater than zero");
        }

        BigDecimal expectedSubtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (expectedSubtotal.compareTo(lineSubtotal) != 0) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ITEM_DATA, "Line subtotal must equal unit price multiplied by quantity");
        }

        BigDecimal expectedTotal = lineSubtotal.subtract(lineDiscount);
        if (expectedTotal.compareTo(lineTotal) != 0) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ITEM_DATA, "Line total must equal line subtotal minus line discount");
        }

        this.productId = productId.trim();
        this.sku = sku.trim();
        this.productName = productName.trim();
        this.slug = slug.trim();
        this.mainImageUrl = normalize(mainImageUrl);
        this.brandId = normalize(brandId);
        this.brandName = normalize(brandName);
        this.categoryId = normalize(categoryId);
        this.categoryName = normalize(categoryName);
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineSubtotal = lineSubtotal;
        this.lineDiscount = lineDiscount;
        this.lineTotal = lineTotal;
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
    }

    void assignTo(Order order) {
        if (order == null) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ITEM_DATA, "Order item must belong to an order");
        }
        this.order = order;
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ITEM_DATA, message);
        }
    }

    private void validateMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ITEM_DATA, message);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
