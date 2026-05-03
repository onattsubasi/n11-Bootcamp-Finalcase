package com.onatsubasi.finalcase.shipment.infrastructure.messaging;

public final class ShipmentEventTypes {

    public static final String SHIPMENT_CREATED = "shipment.created";
    public static final String SHIPMENT_READY_TO_SHIP = "shipment.ready_to_ship";
    public static final String SHIPMENT_SHIPPED = "shipment.shipped";
    public static final String SHIPMENT_IN_TRANSIT = "shipment.in_transit";
    public static final String SHIPMENT_OUT_FOR_DELIVERY = "shipment.out_for_delivery";
    public static final String SHIPMENT_DELIVERED = "shipment.delivered";
    public static final String SHIPMENT_DELIVERY_FAILED = "shipment.delivery_failed";
    public static final String SHIPMENT_CANCELLED = "shipment.cancelled";

    private ShipmentEventTypes() {
    }
}