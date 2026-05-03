package com.onatsubasi.finalcase.shipment.application.dto.response;

import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Shipment summary response")
public record ShipmentSummaryResponse(
        UUID id,
        String shipmentNumber,
        UUID orderId,
        String orderNumber,
        UUID userId,
        ShipmentCarrier carrier,
        ShipmentStatus status,
        String trackingNumber,
        String trackingUrl,
        Instant createdAt,
        Instant shippedAt,
        Instant deliveredAt
) {
}