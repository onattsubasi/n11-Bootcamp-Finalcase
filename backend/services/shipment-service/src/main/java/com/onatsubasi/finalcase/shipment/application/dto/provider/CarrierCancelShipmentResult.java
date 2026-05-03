package com.onatsubasi.finalcase.shipment.application.dto.provider;

import lombok.Builder;

@Builder
public record CarrierCancelShipmentResult(
        boolean success,
        String carrierStatus,
        String failureReason
) {
}