package com.onatsubasi.finalcase.search.application.dto.query;

import com.onatsubasi.finalcase.search.domain.enums.SearchSort;
import com.onatsubasi.finalcase.search.domain.enums.StockStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductSearchCriteria(
        String query,
        UUID categoryId,
        UUID brandId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        StockStatus stockStatus,
        Boolean hasDiscount,
        Map<String, List<String>> attributes,
        SearchSort sort,
        int page,
        int size
) {

    public ProductSearchCriteria {
        query = normalize(query);
        page = Math.max(page, 0);
        size = normalizeSize(size);
        sort = sort == null ? SearchSort.RELEVANCE : sort;

        if (attributes == null) {
            attributes = Map.of();
        }
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
