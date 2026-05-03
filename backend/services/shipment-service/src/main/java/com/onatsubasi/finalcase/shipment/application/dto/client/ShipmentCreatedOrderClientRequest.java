package com.onatsubasi.finalcase.shipment.application.dto.client;

import java.util.UUID;

public record ShipmentCreatedOrderClientRequest(
        UUID shipmentId,
        String shipmentNumber,
        String carrier,
        String trackingNumber,
        String shipmentStatus
) {
}