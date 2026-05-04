package com.onatsubasi.finalcase.catalog.domain.repository;

import java.util.Collections;
import java.util.List;

public record CatalogPage<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public CatalogPage {
        content = content == null ? Collections.emptyList() : List.copyOf(content);
        page = Math.max(page, 0);
        size = Math.max(size, 1);
        totalElements = Math.max(totalElements, 0);
        totalPages = Math.max(totalPages, 0);
    }

    public boolean first() {
        return page == 0;
    }

    public boolean last() {
        return totalPages == 0 || page >= totalPages - 1;
    }

    public boolean empty() {
        return content.isEmpty();
    }
}