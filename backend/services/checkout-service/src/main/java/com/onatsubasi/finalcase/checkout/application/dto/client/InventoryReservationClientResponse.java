package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.time.Instant;
import java.util.UUID;

public record InventoryReservationClientResponse(
        UUID reservationId,
        String status,
        Instant reservedUntil
) {
}