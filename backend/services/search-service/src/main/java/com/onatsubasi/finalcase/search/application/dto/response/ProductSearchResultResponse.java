package com.onatsubasi.finalcase.search.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSearchResultResponse(
        UUID productId,
        String sku,
        String slug,
        String name,
        String description,
        UUID brandId,
        String brandName,
        UUID categoryId,
        String categoryName,
        BigDecimal basePrice,
        BigDecimal discountedPrice,
        BigDecimal effectivePrice,
        String currency,
        String imageUrl,
        int availableQuantity,
        String stockStatus,
        boolean hasDiscount,
        boolean hasActivePromotion,
        String promotionBadge,
        BigDecimal averageRating,
        long reviewCount
) {
}
