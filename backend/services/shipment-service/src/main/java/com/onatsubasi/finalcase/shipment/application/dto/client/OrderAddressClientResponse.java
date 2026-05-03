package com.onatsubasi.finalcase.shipment.application.dto.client;

public record OrderAddressClientResponse(
        String type,
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