package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressSnapshotsClientResponse(
        UUID userId,
        UserAddressSnapshotClientResponse shippingAddress,
        UserAddressSnapshotClientResponse billingAddress,
        Instant snapshottedAt
) {
}
