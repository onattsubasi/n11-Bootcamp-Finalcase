package com.onatsubasi.finalcase.search.application.dto.event;

import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CatalogProductProjectionPayload(
        UUID productId,
        String sku,
        String slug,
        String name,
        String description,
        UUID brandId,
        String brandName,
        UUID categoryId,
        String categoryName,
        List<String> categoryPath,
        BigDecimal basePrice,
        String currency,
        String imageUrl,
        Map<String, Object> attributes,
        List<String> tags,
        ProductSearchStatus status,
        boolean visible,
        Instant sourceUpdatedAt
) {
}
