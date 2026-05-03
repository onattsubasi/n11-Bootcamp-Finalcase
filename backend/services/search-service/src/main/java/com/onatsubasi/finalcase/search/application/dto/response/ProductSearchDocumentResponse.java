package com.onatsubasi.finalcase.search.application.dto.response;

import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.enums.StockStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductSearchDocumentResponse(
        UUID id,
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
        BigDecimal discountedPrice,
        BigDecimal effectivePrice,
        String currency,
        String imageUrl,
        Map<String, Object> attributes,
        List<String> tags,
        int availableQuantity,
        StockStatus stockStatus,
        boolean hasDiscount,
        boolean hasActivePromotion,
        String promotionBadge,
        BigDecimal averageRating,
        long reviewCount,
        ProductSearchStatus status,
        boolean visible,
        Instant sourceUpdatedAt,
        Instant stockUpdatedAt,
        Instant promotionUpdatedAt,
        Instant ratingUpdatedAt,
        Instant indexedAt,
        Instant createdAt,
        Instant updatedAt
) {
}