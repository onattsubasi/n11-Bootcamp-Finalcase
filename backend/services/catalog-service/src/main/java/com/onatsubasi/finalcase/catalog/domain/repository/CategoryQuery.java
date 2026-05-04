package com.onatsubasi.finalcase.catalog.domain.repository;

import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;

import java.util.UUID;

public record CategoryQuery(
        String keyword,
        CatalogStatus status,
        UUID parentId,
        int page,
        int size
) {

    public CategoryQuery {
        keyword = normalize(keyword);
        page = Math.max(page, 0);
        size = normalizeSize(size);
    }

    public static CategoryQuery all(int page, int size) {
        return new CategoryQuery(null, null, null, page, size);
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