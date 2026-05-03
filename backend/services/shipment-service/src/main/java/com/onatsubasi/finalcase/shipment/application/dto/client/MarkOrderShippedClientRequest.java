package com.onatsubasi.finalcase.shipment.application.dto.client;

import java.time.Instant;

public record MarkOrderShippedClientRequest(
        String carrier,
        String trackingNumber,
        Instant shippedAt
) {
}