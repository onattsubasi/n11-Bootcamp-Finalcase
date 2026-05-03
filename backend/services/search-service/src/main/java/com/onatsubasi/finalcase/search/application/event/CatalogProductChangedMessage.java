package com.onatsubasi.finalcase.search.application.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogProductChangedMessage(
        String productId,
        String sku,
        String slug,
        String name,
        String description,
        String brandId,
        String brandName,
        String categoryId,
        String categoryName,
        List<String> categoryPath,
        BigDecimal price,
        String currency,
        String imageUrl,
        Map<String, Object> attributes,
        List<String> tags,
        String status
) {
}