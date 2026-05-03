package com.onatsubasi.finalcase.user.application.dto.response;

import com.onatsubasi.finalcase.user.domain.enums.AddressType;

import java.time.Instant;
import java.util.UUID;

public record UserAddressResponse(
        UUID id,
        UUID userId,
        String title,
        AddressType type,
        String recipientName,
        String phoneNumber,
        String line1,
        String line2,
        String district,
        String city,
        String country,
        String postalCode,
        boolean defaultShipping,
        boolean defaultBilling,
        Instant createdAt,
        Instant updatedAt
) {
}
