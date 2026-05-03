package com.onatsubasi.finalcase.catalog.domain.valueobject;

import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;

public record ProductImage(
        String url,
        int sortOrder,
        boolean main
) {

    public ProductImage {
        if (url == null || url.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product image URL is required"
            );
        }

        if (url.length() > 1000) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product image URL cannot exceed 1000 characters"
            );
        }

        if (sortOrder < 0) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product image sort order cannot be negative"
            );
        }

        url = url.trim();
    }
}