package com.onatsubasi.finalcase.review.application.dto.response;

import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Review summary response")
public record ReviewSummaryResponse(
        UUID id,
        String productId,
        UUID userId,
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
        Instant createdAt,
        Instant updatedAt
) {
}