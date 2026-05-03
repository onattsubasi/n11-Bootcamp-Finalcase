package com.onatsubasi.finalcase.shipment.application.port;

import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;

public interface ShipmentEventPublisher {

    void publishShipmentCreated(Shipment shipment);

    void publishShipmentReadyToShip(Shipment shipment);

    void publishShipmentShipped(Shipment shipment);

    void publishShipmentInTransit(Shipment shipment);

    void publishShipmentOutForDelivery(Shipment shipment);

    void publishShipmentDelivered(Shipment shipment);

    void publishShipmentDeliveryFailed(Shipment shipment);

    void publishShipmentCancelled(Shipment shipment);
}