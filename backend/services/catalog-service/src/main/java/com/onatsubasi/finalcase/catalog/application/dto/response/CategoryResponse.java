package com.onatsubasi.finalcase.catalog.application.dto.response;

import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Category response")
public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        UUID parentId,
        String path,
        int level,
        CatalogStatus status,
        boolean active,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}