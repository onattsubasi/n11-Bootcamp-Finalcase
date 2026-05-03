package com.onatsubasi.finalcase.review.infrastructure.persistence;

import com.onatsubasi.finalcase.review.domain.enums.ReviewReportStatus;
import com.onatsubasi.finalcase.review.domain.entity.ReviewReport;
import com.onatsubasi.finalcase.review.domain.repository.ReviewReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaReviewReportRepositoryAdapter implements ReviewReportRepository {

    private final SpringDataReviewReportJpaRepository springDataRepository;

    @Override
    public ReviewReport save(ReviewReport report) {
        return springDataRepository.save(report);
    }

    @Override
    public Optional<ReviewReport> findById(UUID reportId) {
        return springDataRepository.findById(reportId);
    }

    @Override
    public Optional<ReviewReport> findByIdForUpdate(UUID reportId) {
        return springDataRepository.findByIdForUpdate(reportId);
    }

    @Override
    public Optional<ReviewReport> findByReviewIdAndReporterUserId(
            UUID reviewId,
            UUID reporterUserId
    ) {
        return springDataRepository.findByReviewIdAndReporterUserId(
                reviewId,
                reporterUserId
        );
    }

    @Override
    public boolean existsByReviewIdAndReporterUserId(
            UUID reviewId,
            UUID reporterUserId
    ) {
        return springDataRepository.existsByReviewIdAndReporterUserId(
                reviewId,
                reporterUserId
        );
    }

    @Override
    public Page<ReviewReport> findByStatus(ReviewReportStatus status, Pageable pageable) {
        return springDataRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<ReviewReport> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }
}
