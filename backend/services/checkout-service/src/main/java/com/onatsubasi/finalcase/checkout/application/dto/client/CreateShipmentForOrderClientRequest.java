package com.onatsubasi.finalcase.checkout.application.dto.client;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateShipmentForOrderClientRequest(
        @NotNull(message = "Order id is required")
        UUID orderId
) {
}