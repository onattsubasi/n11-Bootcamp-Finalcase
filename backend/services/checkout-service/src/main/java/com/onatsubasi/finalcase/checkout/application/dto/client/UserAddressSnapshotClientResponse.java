package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.UUID;

public record UserAddressSnapshotClientResponse(
        UUID addressId,
        UUID userId,
        String title,
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