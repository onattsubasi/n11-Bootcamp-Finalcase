package com.onatsubasi.finalcase.catalog.domain.repository;

import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;

public record BrandQuery(
        String keyword,
        CatalogStatus status,
        int page,
        int size
) {

    public BrandQuery {
        keyword = normalize(keyword);
        page = Math.max(page, 0);
        size = normalizeSize(size);
    }

    public static BrandQuery all(int page, int size) {
        return new BrandQuery(null, null, page, size);
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