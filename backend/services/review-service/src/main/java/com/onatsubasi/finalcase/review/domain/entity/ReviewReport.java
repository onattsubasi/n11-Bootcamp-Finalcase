package com.onatsubasi.finalcase.review.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.review.domain.enums.ReviewReportReason;
import com.onatsubasi.finalcase.review.domain.enums.ReviewReportStatus;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "review_reports",
        indexes = {
                @Index(name = "idx_review_reports_review_id", columnList = "review_id"),
                @Index(name = "idx_review_reports_reporter_user_id", columnList = "reporter_user_id"),
                @Index(name = "idx_review_reports_status", columnList = "status"),
                @Index(name = "idx_review_reports_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_reports_review_reporter",
                        columnNames = {"review_id", "reporter_user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "reporter_user_id", nullable = false, updatable = false)
    private UUID reporterUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReviewReportReason reason;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReviewReportStatus status = ReviewReportStatus.OPEN;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private ReviewReport(
            Review review,
            UUID reporterUserId,
            ReviewReportReason reason,
            String description
    ) {
        validateReview(review);
        validateReporterUserId(reporterUserId);
        validateReason(reason);

        if (review.getUserId().equals(reporterUserId)) {
            throw new BaseException(ReviewErrorCode.REVIEW_SELF_REPORT_NOT_ALLOWED);
        }

        this.review = review;
        this.reporterUserId = reporterUserId;
        this.reason = reason;
        this.description = normalize(description, 1000);
        this.status = ReviewReportStatus.OPEN;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;

        review.incrementReportCount();
    }

    public static ReviewReport create(
            Review review,
            UUID reporterUserId,
            ReviewReportReason reason,
            String description
    ) {
        return new ReviewReport(review, reporterUserId, reason, description);
    }

    public boolean resolve(UUID adminUserId, String note) {
        validateAdminUserId(adminUserId);

        if (this.status == ReviewReportStatus.RESOLVED) {
            return false;
        }

        this.status = ReviewReportStatus.RESOLVED;
        this.resolvedBy = adminUserId;
        this.resolvedAt = Instant.now();
        this.resolutionNote = normalize(note, 1000);
        touch();

        return true;
    }

    public boolean dismiss(UUID adminUserId, String note) {
        validateAdminUserId(adminUserId);

        if (this.status == ReviewReportStatus.DISMISSED) {
            return false;
        }

        this.status = ReviewReportStatus.DISMISSED;
        this.resolvedBy = adminUserId;
        this.resolvedAt = Instant.now();
        this.resolutionNote = normalize(note, 1000);
        touch();

        return true;
    }

    private void validateReview(Review review) {
        if (review == null) {
            throw new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }

        if (review.isDeleted()) {
            throw new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }
    }

    private void validateReporterUserId(UUID reporterUserId) {
        if (reporterUserId == null) {
            throw new BaseException(ReviewErrorCode.INVALID_USER_ID);
        }
    }

    private void validateAdminUserId(UUID adminUserId) {
        if (adminUserId == null) {
            throw new BaseException(ReviewErrorCode.INVALID_USER_ID);
        }
    }

    private void validateReason(ReviewReportReason reason) {
        if (reason == null) {
            throw new BaseException(ReviewErrorCode.REVIEW_INVALID_DATA, "Report reason is required");
        }
    }

    private String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return normalized.length() > maxLength
                ? normalized.substring(0, maxLength)
                : normalized;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (status == null) {
            status = ReviewReportStatus.OPEN;
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}