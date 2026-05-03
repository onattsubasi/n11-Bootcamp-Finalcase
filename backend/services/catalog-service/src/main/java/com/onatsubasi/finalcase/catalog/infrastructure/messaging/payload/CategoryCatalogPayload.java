package com.onatsubasi.finalcase.catalog.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;

import java.time.Instant;
import java.util.UUID;

public record CategoryCatalogPayload(
        UUID categoryId,
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

    public static CategoryCatalogPayload from(Category category) {
        return new CategoryCatalogPayload(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParentId(),
                category.getPath(),
                category.getLevel(),
                category.getStatus(),
                category.isActive(),
                category.getSortOrder(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}