package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromotionUsageReservationClientResponse(
        UUID id,
        UUID orderId,
        UUID userId,
        String status,
        Instant reservedUntil,
        List<PromotionUsageReservationItemClientResponse> items
) {
}
