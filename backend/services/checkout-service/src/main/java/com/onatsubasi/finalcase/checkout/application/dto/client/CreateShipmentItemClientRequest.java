package com.onatsubasi.finalcase.checkout.application.dto.client;

public record CreateShipmentItemClientRequest(
        String productId,
        String sku,
        String productName,
        int quantity
) {
}