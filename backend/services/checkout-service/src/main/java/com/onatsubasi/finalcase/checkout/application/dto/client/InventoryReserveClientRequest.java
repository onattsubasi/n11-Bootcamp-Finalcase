package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.List;
import java.util.UUID;

public record InventoryReserveClientRequest(
        UUID checkoutId,
        UUID userId,
        List<InventoryReserveItemClientRequest> items
) {
}
