package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.UUID;

public record ShipmentClientResponse(
        UUID shipmentId,
        String shipmentNumber,
        String carrier,
        String trackingNumber,
        String status
) {
}
