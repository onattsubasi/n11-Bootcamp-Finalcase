package com.onatsubasi.finalcase.checkout.application.dto.client;

public record InventoryReserveItemClientRequest(
        String productId,
        int quantity
) {
}