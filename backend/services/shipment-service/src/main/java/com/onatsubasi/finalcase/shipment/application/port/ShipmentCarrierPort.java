package com.onatsubasi.finalcase.shipment.application.port;

import com.onatsubasi.finalcase.shipment.application.dto.provider.*;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;

public interface ShipmentCarrierPort {

    ShipmentCarrier carrier();

    ShipmentCarrierCapability capability();

    CarrierCreateShipmentResult createShipment(
            CarrierCreateShipmentCommand command
    );

    CarrierCancelShipmentResult cancelShipment(
            CarrierCancelShipmentCommand command
    );

    default boolean supportsCreateShipment() {
        return capability().supportsCreateShipment();
    }

    default boolean supportsCancelShipment() {
        return capability().supportsCancelShipment();
    }
}