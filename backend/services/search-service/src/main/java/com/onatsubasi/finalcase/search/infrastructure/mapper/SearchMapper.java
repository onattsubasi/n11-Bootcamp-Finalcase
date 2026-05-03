package com.onatsubasi.finalcase.search.infrastructure.mapper;

import com.onatsubasi.finalcase.search.application.dto.response.ProductSearchDocumentResponse;
import com.onatsubasi.finalcase.search.application.dto.response.ProductSearchResultResponse;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import org.springframework.stereotype.Component;

@Component
public class SearchMapper {

    public ProductSearchResultResponse toSearchResult(ProductSearchDocument document) {
        return new ProductSearchResultResponse(
                document.getProductId(),
                document.getSku(),
                document.getSlug(),
                document.getName(),
                document.getDescription(),
                document.getBrandId(),
                document.getBrandName(),
                document.getCategoryId(),
                document.getCategoryName(),
                document.getBasePrice(),
                document.getDiscountedPrice(),
                document.effectivePrice(),
                document.getCurrency(),
                document.getImageUrl(),
                document.getAvailableQuantity(),
                document.getStockStatus().name(),
                document.isHasDiscount(),
                document.isHasActivePromotion(),
                document.getPromotionBadge(),
                document.getAverageRating(),
                document.getReviewCount()
        );
    }

    public ProductSearchDocumentResponse toDocumentResponse(ProductSearchDocument document) {
        return new ProductSearchDocumentResponse(
                document.getId(),
                document.getProductId(),
                document.getSku(),
                document.getSlug(),
                document.getName(),
                document.getDescription(),
                document.getBrandId(),
                document.getBrandName(),
                document.getCategoryId(),
                document.getCategoryName(),
                document.getCategoryPath(),
                document.getBasePrice(),
                document.getDiscountedPrice(),
                document.effectivePrice(),
                document.getCurrency(),
                document.getImageUrl(),
                document.getAttributes(),
                document.getTags(),
                document.getAvailableQuantity(),
                document.getStockStatus(),
                document.isHasDiscount(),
                document.isHasActivePromotion(),
                document.getPromotionBadge(),
                document.getAverageRating(),
                document.getReviewCount(),
                document.getStatus(),
                document.isVisible(),
                document.getSourceUpdatedAt(),
                document.getStockUpdatedAt(),
                document.getPromotionUpdatedAt(),
                document.getRatingUpdatedAt(),
                document.getIndexedAt(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}