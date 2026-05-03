package com.onatsubasi.finalcase.search.application.dto.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RatingSummaryProjectionPayload(
        UUID productId,
        BigDecimal averageRating,
        long reviewCount,
        Instant ratingUpdatedAt,
        Instant updatedAt
) {

    public Instant effectiveUpdatedAt() {
        return ratingUpdatedAt != null ? ratingUpdatedAt : updatedAt;
    }
}
