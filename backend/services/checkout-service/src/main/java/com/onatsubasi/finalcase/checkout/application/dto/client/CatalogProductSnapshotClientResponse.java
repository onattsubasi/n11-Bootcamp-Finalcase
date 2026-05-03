package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;

public record CatalogProductSnapshotClientResponse(
        String productId,
        String sku,
        String slug,
        String name,
        String description,
        String brandId,
        String brandName,
        String categoryId,
        String categoryName,
        String mainImageUrl,
        BigDecimal price,
        String currency,
        boolean active
) {
}