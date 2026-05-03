package com.onatsubasi.finalcase.catalog.domain.valueobject;

import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandSnapshot {

    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "slug", nullable = false, length = 140)
    private String slug;

    public BrandSnapshot(UUID id, String name, String slug) {
        if (id == null) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Brand snapshot id is required"
            );
        }

        if (name == null || name.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Brand snapshot name is required"
            );
        }

        if (slug == null || slug.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Brand snapshot slug is required"
            );
        }

        this.id = id;
        this.name = name.trim();
        this.slug = slug.trim();
    }

    public static BrandSnapshot from(Brand brand) {
        return new BrandSnapshot(
                brand.getId(),
                brand.getName(),
                brand.getSlug()
        );
    }
}