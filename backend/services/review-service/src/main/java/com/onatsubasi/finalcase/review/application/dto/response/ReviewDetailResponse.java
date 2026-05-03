package com.onatsubasi.finalcase.review.application.dto.response;

import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Review detail response")
public record ReviewDetailResponse(
        UUID id,
        String productId,
        UUID userId,
        UUID orderId,
        UUID orderItemId,
        String orderNumber,
        String authorDisplayName,
        int rating,
        String title,
        String comment,
        List<Map<String, Object>> images,
        ReviewStatus status,
        boolean visible,
        boolean verifiedPurchase,
        int helpfulCount,
        int unhelpfulCount,
        int reportCount,
        Map<String, Object> moderationMetadata,
        Instant approvedAt,
        Instant rejectedAt,
        Instant hiddenAt,
        Instant deletedAt,
        Instant createdAt,
        Instant updatedAt
) {
}