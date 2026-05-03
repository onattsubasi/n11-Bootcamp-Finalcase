package com.onatsubasi.finalcase.shipment.application.dto.response;

import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Shipment detail response")
public record ShipmentDetailResponse(
        UUID id,
        String shipmentNumber,
        UUID orderId,
        String orderNumber,
        UUID userId,

        ShipmentCarrier carrier,
        ShipmentStatus status,

        String carrierShipmentId,
        String trackingNumber,
        String trackingUrl,
        String labelUrl,
        String carrierStatus,
        String failureReason,

        ShipmentAddressResponse shippingAddress,
        List<ShipmentItemResponse> items,
        List<ShipmentStatusHistoryResponse> statusHistory,

        Instant createdAt,
        Instant updatedAt,
        Instant readyToShipAt,
        Instant shippedAt,
        Instant inTransitAt,
        Instant outForDeliveryAt,
        Instant deliveredAt,
        Instant deliveryFailedAt,
        Instant cancelledAt
) {
}