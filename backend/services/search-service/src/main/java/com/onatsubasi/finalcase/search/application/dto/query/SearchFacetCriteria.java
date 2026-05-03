package com.onatsubasi.finalcase.search.application.dto.query;

import com.onatsubasi.finalcase.search.domain.enums.StockStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SearchFacetCriteria(
        String query,
        UUID categoryId,
        UUID brandId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        StockStatus stockStatus,
        Boolean hasDiscount,
        Map<String, List<String>> attributes
) {

    public SearchFacetCriteria {
        query = query == null || query.isBlank() ? null : query.trim();

        if (attributes == null) {
            attributes = Map.of();
        }
    }
}
