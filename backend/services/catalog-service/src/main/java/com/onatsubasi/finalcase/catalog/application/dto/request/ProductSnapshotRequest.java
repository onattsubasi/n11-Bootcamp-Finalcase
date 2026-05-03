package com.onatsubasi.finalcase.catalog.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(description = "Internal request to retrieve authoritative product snapshots")
public record ProductSnapshotRequest(

        @NotEmpty
        @Schema(
                description = "Product ids requested by Checkout or other internal services",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<UUID> productIds
) {
}