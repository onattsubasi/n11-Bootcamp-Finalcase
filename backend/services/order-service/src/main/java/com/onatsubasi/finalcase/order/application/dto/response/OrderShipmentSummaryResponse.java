package com.onatsubasi.finalcase.order.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OrderShipmentSummaryResponse(
        UUID shipmentId,
        String shipmentNumber,
        String carrier,
        String trackingNumber,
        String shipmentStatus,
        Instant shippedAt,
        Instant deliveredAt
) {
}