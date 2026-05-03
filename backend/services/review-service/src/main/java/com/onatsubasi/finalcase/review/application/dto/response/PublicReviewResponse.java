package com.onatsubasi.finalcase.review.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicReviewResponse(
        UUID reviewId,
        UUID productId,
        String authorDisplayName,
        int rating,
        String title,
        String comment,
        List<ReviewImageResponse> images,
        boolean verifiedPurchase,
        int helpfulCount,
        int unhelpfulCount,
        Instant createdAt
) {
}
