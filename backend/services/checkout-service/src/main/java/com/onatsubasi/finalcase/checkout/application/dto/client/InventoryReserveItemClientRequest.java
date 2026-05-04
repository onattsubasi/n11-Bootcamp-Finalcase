package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.UUID;

public record InventoryReserveItemClientRequest(
        UUID productId,
        int quantity
) {
}
