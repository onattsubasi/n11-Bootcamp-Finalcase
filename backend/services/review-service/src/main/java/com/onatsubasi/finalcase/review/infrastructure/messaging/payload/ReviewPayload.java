package com.onatsubasi.finalcase.review.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import com.onatsubasi.finalcase.review.domain.entity.Review;

import java.time.Instant;
import java.util.UUID;

public record ReviewPayload(
        UUID reviewId,
        UUID productId,
        UUID userId,
        UUID orderId,
        UUID orderItemId,
        String orderNumber,
        int rating,
        ReviewStatus status,
        boolean visible,
        boolean verifiedPurchase,
        int helpfulCount,
        int unhelpfulCount,
        int reportCount,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {

    public static ReviewPayload from(Review review) {
        return new ReviewPayload(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                review.getOrderId(),
                review.getOrderItemId(),
                review.getOrderNumber(),
                review.getRating(),
                review.getStatus(),
                review.isVisible(),
                review.isVerifiedPurchase(),
                review.getHelpfulCount(),
                review.getUnhelpfulCount(),
                review.getReportCount(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                review.getDeletedAt()
        );
    }
}
