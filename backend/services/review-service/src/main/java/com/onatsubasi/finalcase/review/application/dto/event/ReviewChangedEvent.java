package com.onatsubasi.finalcase.review.application.dto.event;

import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ReviewChangedEvent(
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
        Instant updatedAt
) {

    public static ReviewChangedEvent from(Review review) {
        return ReviewChangedEvent.builder()
                .reviewId(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .orderId(review.getOrderId())
                .orderItemId(review.getOrderItemId())
                .orderNumber(review.getOrderNumber())
                .rating(review.getRating())
                .status(review.getStatus())
                .visible(review.isVisible())
                .verifiedPurchase(review.isVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .unhelpfulCount(review.getUnhelpfulCount())
                .reportCount(review.getReportCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}