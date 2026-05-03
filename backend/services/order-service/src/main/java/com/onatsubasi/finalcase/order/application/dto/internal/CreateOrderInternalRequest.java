package com.onatsubasi.finalcase.order.application.dto.internal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderInternalRequest(
        @NotNull(message = "Checkout id is required")
        UUID checkoutId,

        @Size(max = 120, message = "Idempotency key cannot exceed 120 characters")
        String idempotencyKey,

        @NotBlank(message = "Request hash is required")
        @Size(max = 128, message = "Request hash cannot exceed 128 characters")
        String requestHash,

        @NotNull(message = "User id is required")
        UUID userId,

        UUID basketId,

        UUID inventoryReservationId,

        UUID promotionUsageReservationId,

        @NotNull(message = "Shipping address is required")
        @Valid
        OrderAddressSnapshotRequest shippingAddress,

        @NotNull(message = "Billing address is required")
        @Valid
        OrderAddressSnapshotRequest billingAddress,

        @NotNull(message = "Subtotal amount is required")
        @DecimalMin(value = "0.00", message = "Subtotal amount cannot be negative")
        BigDecimal subtotalAmount,

        @NotNull(message = "Item discount amount is required")
        @DecimalMin(value = "0.00", message = "Item discount amount cannot be negative")
        BigDecimal itemDiscountAmount,

        @NotNull(message = "Promotion discount amount is required")
        @DecimalMin(value = "0.00", message = "Promotion discount amount cannot be negative")
        BigDecimal promotionDiscountAmount,

        @NotNull(message = "Shipping fee is required")
        @DecimalMin(value = "0.00", message = "Shipping fee cannot be negative")
        BigDecimal shippingFee,

        @NotNull(message = "Shipping discount amount is required")
        @DecimalMin(value = "0.00", message = "Shipping discount amount cannot be negative")
        BigDecimal shippingDiscountAmount,

        @NotNull(message = "Tax amount is required")
        @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
        BigDecimal taxAmount,

        @NotNull(message = "Grand total amount is required")
        @DecimalMin(value = "0.00", message = "Grand total amount cannot be negative")
        BigDecimal grandTotalAmount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        String currency,

        @NotNull(message = "Order items are required")
        @Size(min = 1, message = "At least one order item is required")
        List<@Valid CreateOrderItemRequest> items,

        List<@Valid CreateOrderDiscountRequest> discounts
) {
}