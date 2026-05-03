package com.onatsubasi.finalcase.shipment.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Shipment address snapshot response")
public record ShipmentAddressResponse(
        String recipientName,
        String recipientPhone,
        String country,
        String city,
        String district,
        String neighborhood,
        String addressLine1,
        String addressLine2,
        String postalCode
) {
}