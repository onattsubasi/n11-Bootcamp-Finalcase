package com.onatsubasi.finalcase.catalog.application.dto.response;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductSummaryResponse(
        String id,
        String sku,
        String name,
        String slug,
        BigDecimal priceAmount,
        String currency,
        String brandId,
        String brandName,
        String categoryId,
        String categoryName,
        String mainImageUrl,
        ProductStatus status,
        Instant updatedAt
) {
}