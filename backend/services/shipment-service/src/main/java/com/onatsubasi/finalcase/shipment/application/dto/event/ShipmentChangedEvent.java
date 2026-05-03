package com.onatsubasi.finalcase.shipment.application.dto.event;

import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ShipmentChangedEvent(
        UUID shipmentId,
        String shipmentNumber,
        UUID orderId,
        String orderNumber,
        UUID userId,
        ShipmentCarrier carrier,
        ShipmentStatus status,
        String trackingNumber,
        String trackingUrl,
        Instant shippedAt,
        Instant deliveredAt
) {

    public static ShipmentChangedEvent from(Shipment shipment) {
        return ShipmentChangedEvent.builder()
                .shipmentId(shipment.getId())
                .shipmentNumber(shipment.getShipmentNumber())
                .orderId(shipment.getOrderId())
                .orderNumber(shipment.getOrderNumber())
                .userId(shipment.getUserId())
                .carrier(shipment.getCarrier())
                .status(shipment.getStatus())
                .trackingNumber(shipment.getTrackingNumber())
                .trackingUrl(shipment.getTrackingUrl())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .build();
    }
}