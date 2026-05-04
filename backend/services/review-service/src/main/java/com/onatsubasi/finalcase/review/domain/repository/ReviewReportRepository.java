package com.onatsubasi.finalcase.review.domain.repository;

import com.onatsubasi.finalcase.review.domain.enums.ReviewReportStatus;
import com.onatsubasi.finalcase.review.domain.entity.ReviewReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReviewReportRepository {

    ReviewReport save(ReviewReport report);

    Optional<ReviewReport> findById(UUID reportId);

    Optional<ReviewReport> findByIdForUpdate(UUID reportId);

    Optional<ReviewReport> findByReviewIdAndReporterUserId(
            UUID reviewId,
            UUID reporterUserId
    );

    boolean existsByReviewIdAndReporterUserId(
            UUID reviewId,
            UUID reporterUserId
    );

    Page<ReviewReport> findByStatus(ReviewReportStatus status, Pageable pageable);

    Page<ReviewReport> findAll(Pageable pageable);
}