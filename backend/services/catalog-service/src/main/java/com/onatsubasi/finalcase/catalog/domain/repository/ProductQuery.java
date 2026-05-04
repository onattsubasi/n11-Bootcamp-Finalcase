package com.onatsubasi.finalcase.catalog.domain.repository;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;

import java.util.UUID;

public record ProductQuery(
        String keyword,
        ProductStatus status,
        UUID categoryId,
        UUID brandId,
        String storeId,
        int page,
        int size
) {

    public ProductQuery {
        keyword = normalize(keyword);
        storeId = normalize(storeId);
        page = Math.max(page, 0);
        size = normalizeSize(size);
    }

    public static ProductQuery all(int page, int size) {
        return new ProductQuery(null, null, null, null, null, page, size);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private static int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }
}