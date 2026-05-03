package com.onatsubasi.finalcase.catalog.domain.valueobject;

import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.common.core.exception.BaseException;

import java.util.UUID;

public record CategoryAncestor(
        UUID id,
        String name,
        String slug
) {

    public CategoryAncestor {
        if (id == null) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Ancestor category id is required"
            );
        }

        if (name == null || name.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Ancestor category name is required"
            );
        }

        if (slug == null || slug.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Ancestor category slug is required"
            );
        }

        name = name.trim();
        slug = slug.trim();
    }

    public static CategoryAncestor from(Category category) {
        return new CategoryAncestor(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }
}