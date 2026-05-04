package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserAddressSnapshotClientResponse(
        UUID addressId,
        UUID userId,
        String title,
        String recipientName,
        @JsonAlias("phoneNumber")
        String recipientPhone,
        String country,
        String city,
        String district,
        String neighborhood,
        @JsonAlias("line1")
        String addressLine1,
        @JsonAlias("line2")
        String addressLine2,
        String postalCode
) {
}
