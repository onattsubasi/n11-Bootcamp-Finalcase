package com.onatsubasi.finalcase.user.application.dto.internal;

import com.onatsubasi.finalcase.user.domain.enums.AddressType;

import java.util.UUID;

public record AddressSnapshot(
        UUID addressId,
        AddressType type,
        String title,
        String recipientName,
        String phoneNumber,
        String line1,
        String line2,
        String district,
        String city,
        String country,
        String postalCode
) {
}
