package com.onatsubasi.finalcase.catalog.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;
import com.onatsubasi.finalcase.catalog.domain.entity.Brand;

import java.time.Instant;
import java.util.UUID;

public record BrandCatalogPayload(
        UUID brandId,
        String name,
        String slug,
        String description,
        String logoUrl,
        CatalogStatus status,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static BrandCatalogPayload from(Brand brand) {
        return new BrandCatalogPayload(
                brand.getId(),
                brand.getName(),
                brand.getSlug(),
                brand.getDescription(),
                brand.getLogoUrl(),
                brand.getStatus(),
                brand.isActive(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }
}