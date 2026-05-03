package com.onatsubasi.finalcase.shipment.infrastructure.carrier.manual;

import com.onatsubasi.finalcase.shipment.application.dto.provider.*;
import com.onatsubasi.finalcase.shipment.application.port.ShipmentCarrierPort;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ManualShipmentCarrierAdapter implements ShipmentCarrierPort {

    @Override
    public ShipmentCarrier carrier() {
        return ShipmentCarrier.MANUAL;
    }

    @Override
    public ShipmentCarrierCapability capability() {
        return ShipmentCarrierCapability.builder()
                .carrier(ShipmentCarrier.MANUAL)
                .supportsCreateShipment(true)
                .supportsCancelShipment(true)
                .supportsTracking(true)
                .supportsLabel(false)
                .build();
    }

    @Override
    public CarrierCreateShipmentResult createShipment(
            CarrierCreateShipmentCommand command
    ) {
        log.info(
                "event=shipment.manual_create_requested shipmentId={} orderId={} shipmentNumber={}",
                command.shipmentId(),
                command.orderId(),
                command.shipmentNumber()
        );

        return CarrierCreateShipmentResult.builder()
                .success(true)
                .carrierShipmentId(command.shipmentId().toString())
                .trackingNumber(null)
                .trackingUrl(null)
                .labelUrl(null)
                .carrierStatus("MANUAL_CREATED")
                .failureReason(null)
                .build();
    }

    @Override
    public CarrierCancelShipmentResult cancelShipment(
            CarrierCancelShipmentCommand command
    ) {
        log.info(
                "event=shipment.manual_cancel_requested shipmentId={} shipmentNumber={}",
                command.shipmentId(),
                command.shipmentNumber()
        );

        return CarrierCancelShipmentResult.builder()
                .success(true)
                .carrierStatus("MANUAL_CANCELLED")
                .failureReason(null)
                .build();
    }
}
