package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.UUID;

public record AddressSnapshotClientRequest(
        UUID userId,
        UUID shippingAddressId,
        UUID billingAddressId
) {
}
