package com.onatsubasi.finalcase.review.application.dto.response;

import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CustomerReviewResponse(
        UUID reviewId,
        UUID productId,
        UUID orderId,
        UUID orderItemId,
        String orderNumber,
        int rating,
        String title,
        String comment,
        List<ReviewImageResponse> images,
        ReviewStatus status,
        boolean visible,
        boolean verifiedPurchase,
        int helpfulCount,
        int unhelpfulCount,
        int reportCount,
        Instant deliveredAt,
        Instant createdAt,
        Instant updatedAt
) {
}
