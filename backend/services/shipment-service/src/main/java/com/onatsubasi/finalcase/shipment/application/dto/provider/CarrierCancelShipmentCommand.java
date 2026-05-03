package com.onatsubasi.finalcase.shipment.application.dto.provider;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CarrierCancelShipmentCommand(
        UUID shipmentId,
        String shipmentNumber,
        String carrierShipmentId,
        String trackingNumber,
        String reason
) {
}