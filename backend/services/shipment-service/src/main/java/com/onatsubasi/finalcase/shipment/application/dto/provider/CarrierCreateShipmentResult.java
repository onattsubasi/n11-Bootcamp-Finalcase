package com.onatsubasi.finalcase.shipment.application.dto.provider;

import lombok.Builder;

@Builder
public record CarrierCreateShipmentResult(
        boolean success,
        String carrierShipmentId,
        String trackingNumber,
        String trackingUrl,
        String labelUrl,
        String carrierStatus,
        String failureReason
) {
}