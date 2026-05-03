package com.onatsubasi.finalcase.search.application.dto.request;

import com.onatsubasi.finalcase.search.domain.enums.SearchSort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductSearchRequest(
        String q,
        String categoryId,
        List<String> brandIds,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock,
        Boolean hasPromotion,
        Map<String, String> attributes,
        SearchSort sort,
        Integer page,
        Integer size
) {
    public int safePage() {
        return page == null || page < 0 ? 0 : page;
    }

    public int safeSize() {
        if (size == null || size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }

    public SearchSort safeSort() {
        return sort == null ? SearchSort.RELEVANCE : sort;
    }

    public String normalizedQuery() {
        return q == null || q.isBlank()
                ? null
                : q.trim();
    }
}