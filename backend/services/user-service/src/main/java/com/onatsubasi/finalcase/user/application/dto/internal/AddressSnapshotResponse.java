package com.onatsubasi.finalcase.user.application.dto.internal;

import java.time.Instant;
import java.util.UUID;

public record AddressSnapshotResponse(
        UUID userId,
        AddressSnapshot shippingAddress,
        AddressSnapshot billingAddress,
        Instant snapshottedAt
) {
}
