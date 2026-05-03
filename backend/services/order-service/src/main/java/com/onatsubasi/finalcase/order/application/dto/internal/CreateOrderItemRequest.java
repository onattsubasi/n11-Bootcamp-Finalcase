package com.onatsubasi.finalcase.order.application.dto.internal;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotBlank(message = "Product id is required")
        String productId,

        @NotBlank(message = "SKU is required")
        @Size(max = 120, message = "SKU cannot exceed 120 characters")
        String sku,

        @NotBlank(message = "Product name is required")
        @Size(max = 250, message = "Product name cannot exceed 250 characters")
        String productName,

        @NotBlank(message = "Slug is required")
        @Size(max = 180, message = "Slug cannot exceed 180 characters")
        String slug,

        @Size(max = 1000, message = "Main image url cannot exceed 1000 characters")
        String mainImageUrl,

        @Size(max = 100, message = "Brand id cannot exceed 100 characters")
        String brandId,

        @Size(max = 150, message = "Brand name cannot exceed 150 characters")
        String brandName,

        @Size(max = 100, message = "Category id cannot exceed 100 characters")
        String categoryId,

        @Size(max = 150, message = "Category name cannot exceed 150 characters")
        String categoryName,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
        BigDecimal unitPrice,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @NotNull(message = "Line subtotal is required")
        @DecimalMin(value = "0.00", message = "Line subtotal cannot be negative")
        BigDecimal lineSubtotal,

        @NotNull(message = "Line discount is required")
        @DecimalMin(value = "0.00", message = "Line discount cannot be negative")
        BigDecimal lineDiscount,

        @NotNull(message = "Line total is required")
        @DecimalMin(value = "0.00", message = "Line total cannot be negative")
        BigDecimal lineTotal,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        String currency
) {
}
