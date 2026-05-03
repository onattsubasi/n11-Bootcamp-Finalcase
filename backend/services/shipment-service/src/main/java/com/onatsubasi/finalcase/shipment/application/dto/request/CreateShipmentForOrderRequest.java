package com.onatsubasi.finalcase.shipment.application.dto.request;

import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Internal request to create shipment for an order")
public record CreateShipmentForOrderRequest(
        @NotNull(message = "Order id is required")
        UUID orderId,

        @Schema(description = "Optional carrier override. If null, service default carrier is used.")
        ShipmentCarrier carrier
) {
}