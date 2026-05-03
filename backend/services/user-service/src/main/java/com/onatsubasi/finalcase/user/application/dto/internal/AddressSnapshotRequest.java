package com.onatsubasi.finalcase.user.application.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Internal request to create immutable address snapshots for checkout")
public record AddressSnapshotRequest(

        @NotNull(message = "userId is required")
        UUID userId,

        @NotNull(message = "shippingAddressId is required")
        UUID shippingAddressId,

        UUID billingAddressId
) {
}
