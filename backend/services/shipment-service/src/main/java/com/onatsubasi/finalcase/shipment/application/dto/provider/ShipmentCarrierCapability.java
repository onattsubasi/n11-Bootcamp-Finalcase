package com.onatsubasi.finalcase.shipment.application.dto.provider;

import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import lombok.Builder;

@Builder
public record ShipmentCarrierCapability(
        ShipmentCarrier carrier,
        boolean supportsCreateShipment,
        boolean supportsCancelShipment,
        boolean supportsTracking,
        boolean supportsLabel
) {
}