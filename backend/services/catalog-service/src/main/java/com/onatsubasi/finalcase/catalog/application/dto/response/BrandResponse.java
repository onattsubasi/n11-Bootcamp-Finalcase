package com.onatsubasi.finalcase.catalog.application.dto.response;

import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Brand response")
public record BrandResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String logoUrl,
        CatalogStatus status,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}