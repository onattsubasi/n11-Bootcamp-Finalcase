package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PromotionUsageReservationClientResponse(
        UUID id,
        UUID checkoutId,
        UUID orderId,
        UUID userId,
        String status,
        Instant reservedUntil,
        List<PromotionUsageReservationItemClientResponse> items
) {
}
