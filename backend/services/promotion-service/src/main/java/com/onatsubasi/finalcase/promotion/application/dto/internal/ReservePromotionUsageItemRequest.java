package com.onatsubasi.finalcase.promotion.application.dto.internal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservePromotionUsageItemRequest(
        @NotNull(message = "Promotion id is required")
        UUID promotionId,

        UUID couponId,

        String couponCode,

        @NotNull(message = "Discount amount is required")
        @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
        BigDecimal discountAmount,

        @NotNull(message = "Shipping discount amount is required")
        @DecimalMin(value = "0.00", message = "Shipping discount amount cannot be negative")
        BigDecimal shippingDiscountAmount,

        String description
) {
}