package com.onatsubasi.finalcase.review.application.dto.response;

import com.onatsubasi.finalcase.review.domain.enums.ReviewReportReason;
import com.onatsubasi.finalcase.review.domain.enums.ReviewReportStatus;

import java.time.Instant;
import java.util.UUID;

public record ReviewReportResponse(
        UUID reportId,
        UUID reviewId,
        UUID reporterUserId,
        ReviewReportReason reason,
        String description,
        ReviewReportStatus status,
        UUID resolvedBy,
        Instant resolvedAt,
        String resolutionNote,
        Instant createdAt,
        Instant updatedAt
) {
}