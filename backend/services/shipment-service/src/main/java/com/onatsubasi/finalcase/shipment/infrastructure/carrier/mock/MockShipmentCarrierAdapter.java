package com.onatsubasi.finalcase.shipment.infrastructure.carrier.mock;

import com.onatsubasi.finalcase.shipment.application.dto.provider.*;
import com.onatsubasi.finalcase.shipment.application.port.ShipmentCarrierPort;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
public class MockShipmentCarrierAdapter implements ShipmentCarrierPort {

    @Override
    public ShipmentCarrier carrier() {
        return ShipmentCarrier.MOCK;
    }

    @Override
    public ShipmentCarrierCapability capability() {
        return ShipmentCarrierCapability.builder()
                .carrier(ShipmentCarrier.MOCK)
                .supportsCreateShipment(true)
                .supportsCancelShipment(true)
                .supportsTracking(true)
                .supportsLabel(true)
                .build();
    }

    @Override
    public CarrierCreateShipmentResult createShipment(
            CarrierCreateShipmentCommand command
    ) {
        String trackingNumber = "MOCK-" +
                command.shipmentNumber()
                        .replace("-", "")
                        .toUpperCase(Locale.ROOT);

        String trackingUrl = "https://mock-carrier.local/track/" + trackingNumber;

        log.info(
                "event=shipment.mock_create_requested shipmentId={} orderId={} trackingNumber={}",
                command.shipmentId(),
                command.orderId(),
                trackingNumber
        );

        return CarrierCreateShipmentResult.builder()
                .success(true)
                .carrierShipmentId("MOCK-CARRIER-" + command.shipmentId())
                .trackingNumber(trackingNumber)
                .trackingUrl(trackingUrl)
                .labelUrl("https://mock-carrier.local/labels/" + trackingNumber + ".pdf")
                .carrierStatus("MOCK_CREATED")
                .failureReason(null)
                .build();
    }

    @Override
    public CarrierCancelShipmentResult cancelShipment(
            CarrierCancelShipmentCommand command
    ) {
        log.info(
                "event=shipment.mock_cancel_requested shipmentId={} trackingNumber={}",
                command.shipmentId(),
                command.trackingNumber()
        );

        return CarrierCancelShipmentResult.builder()
                .success(true)
                .carrierStatus("MOCK_CANCELLED")
                .failureReason(null)
                .build();
    }
}