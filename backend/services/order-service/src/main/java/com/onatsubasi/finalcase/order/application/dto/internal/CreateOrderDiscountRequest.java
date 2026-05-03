package com.onatsubasi.finalcase.order.application.dto.internal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderDiscountRequest(
        UUID promotionId,

        @Size(max = 200, message = "Promotion name cannot exceed 200 characters")
        String promotionName,

        UUID couponId,

        @Size(max = 80, message = "Coupon code cannot exceed 80 characters")
        String couponCode,

        @NotNull(message = "Discount amount is required")
        @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
        BigDecimal discountAmount,

        @NotNull(message = "Shipping discount amount is required")
        @DecimalMin(value = "0.00", message = "Shipping discount amount cannot be negative")
        BigDecimal shippingDiscountAmount
) {
}