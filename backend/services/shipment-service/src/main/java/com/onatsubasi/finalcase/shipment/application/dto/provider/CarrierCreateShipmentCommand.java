package com.onatsubasi.finalcase.shipment.application.dto.provider;

import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentAddressSnapshot;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentItem;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CarrierCreateShipmentCommand(
        UUID shipmentId,
        String shipmentNumber,
        UUID orderId,
        String orderNumber,
        UUID userId,
        ShipmentCarrier carrier,
        ShipmentAddressSnapshot shippingAddress,
        List<ShipmentItem> items
) {
}