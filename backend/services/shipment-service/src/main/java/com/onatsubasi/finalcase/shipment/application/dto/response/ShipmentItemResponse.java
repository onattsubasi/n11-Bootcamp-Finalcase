package com.onatsubasi.finalcase.shipment.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Shipment item response")
public record ShipmentItemResponse(
        UUID id,
        String productId,
        String sku,
        String productName,
        int quantity
) {
}