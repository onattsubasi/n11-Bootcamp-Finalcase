package com.onatsubasi.finalcase.review.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.review.domain.enums.ReviewReportReason;
import com.onatsubasi.finalcase.review.domain.enums.ReviewReportStatus;
import com.onatsubasi.finalcase.review.domain.entity.ReviewReport;

import java.time.Instant;
import java.util.UUID;

public record ReviewReportPayload(
        UUID reportId,
        UUID reviewId,
        UUID productId,
        UUID reporterUserId,
        ReviewReportReason reason,
        ReviewReportStatus status,
        UUID resolvedBy,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static ReviewReportPayload from(ReviewReport report) {
        return new ReviewReportPayload(
                report.getId(),
                report.getReview().getId(),
                report.getReview().getProductId(),
                report.getReporterUserId(),
                report.getReason(),
                report.getStatus(),
                report.getResolvedBy(),
                report.getResolvedAt(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
