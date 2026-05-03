package com.onatsubasi.finalcase.review.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductRatingSummaryResponse(
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
}