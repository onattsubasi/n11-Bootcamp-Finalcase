package com.onatsubasi.finalcase.checkout.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckoutQuoteRequest(
        @NotNull(message = "Basket id is required")
        UUID basketId,

        @NotNull(message = "Shipping address id is required")
        UUID shippingAddressId,

        UUID billingAddressId,

        String couponCode
) {
}