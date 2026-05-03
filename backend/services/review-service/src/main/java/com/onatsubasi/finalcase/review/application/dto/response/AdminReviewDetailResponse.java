package com.onatsubasi.finalcase.review.application.dto.response;

import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AdminReviewDetailResponse(
        UUID reviewId,
        UUID productId,
        UUID userId,
        UUID orderId,
        UUID orderItemId,
        String orderNumber,
        String authorDisplayName,
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
        Map<String, Object> moderationMetadata,
        UUID lastModeratedBy,
        Instant lastModeratedAt,
        Instant approvedAt,
        Instant rejectedAt,
        Instant hiddenAt,
        Instant deletedAt,
        Instant deliveredAt,
        Instant createdAt,
        Instant updatedAt
) {
}
