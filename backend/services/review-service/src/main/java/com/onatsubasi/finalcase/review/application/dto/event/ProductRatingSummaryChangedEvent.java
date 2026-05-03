package com.onatsubasi.finalcase.review.application.dto.event;

import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ProductRatingSummaryChangedEvent(
        UUID summaryId,
        UUID productId,
        BigDecimal averageRating,
        long reviewCount,
        long rating1Count,
        long rating2Count,
        long rating3Count,
        long rating4Count,
        long rating5Count,
        Instant updatedAt
) {

    public static ProductRatingSummaryChangedEvent from(ProductRatingSummary summary) {
        return ProductRatingSummaryChangedEvent.builder()
                .summaryId(summary.getId())
                .productId(summary.getProductId())
                .averageRating(summary.getAverageRating())
                .reviewCount(summary.getReviewCount())
                .rating1Count(summary.getRating1Count())
                .rating2Count(summary.getRating2Count())
                .rating3Count(summary.getRating3Count())
                .rating4Count(summary.getRating4Count())
                .rating5Count(summary.getRating5Count())
                .updatedAt(summary.getUpdatedAt())
                .build();
    }
}