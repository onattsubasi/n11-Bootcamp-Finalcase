package com.onatsubasi.finalcase.review.infrastructure.persistence;

import com.onatsubasi.finalcase.review.domain.enums.ReviewReportStatus;
import com.onatsubasi.finalcase.review.domain.entity.ReviewReport;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataReviewReportJpaRepository extends JpaRepository<ReviewReport, UUID> {

    @EntityGraph(attributePaths = "review")
    Optional<ReviewReport> findByReviewIdAndReporterUserId(UUID reviewId, UUID reporterUserId);

    boolean existsByReviewIdAndReporterUserId(UUID reviewId, UUID reporterUserId);

    Page<ReviewReport> findByStatus(ReviewReportStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "review")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r
             from ReviewReport r
            where r.id = :reportId
           """)
    Optional<ReviewReport> findByIdForUpdate(@Param("reportId") UUID reportId);
}