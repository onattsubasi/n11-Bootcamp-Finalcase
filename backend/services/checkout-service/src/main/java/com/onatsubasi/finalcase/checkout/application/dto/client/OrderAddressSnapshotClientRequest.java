package com.onatsubasi.finalcase.checkout.application.dto.client;

public record OrderAddressSnapshotClientRequest(
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
