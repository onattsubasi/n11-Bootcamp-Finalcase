package com.onatsubasi.finalcase.review.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RatingSummaryPayload(
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

    public static RatingSummaryPayload from(ProductRatingSummary summary) {
        return new RatingSummaryPayload(
                summary.getProductId(),
                summary.getAverageRating(),
                summary.getReviewCount(),
                summary.getRating1Count(),
                summary.getRating2Count(),
                summary.getRating3Count(),
                summary.getRating4Count(),
                summary.getRating5Count(),
                summary.getUpdatedAt()
        );
    }
}
