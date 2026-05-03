package com.onatsubasi.finalcase.review.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.review.application.dto.request.ReportReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.ResolveReviewReportRequest;
import com.onatsubasi.finalcase.review.application.dto.response.PageResponse;
import com.onatsubasi.finalcase.review.application.dto.response.ReviewReportResponse;
import com.onatsubasi.finalcase.review.application.port.ReviewEventPublisher;
import com.onatsubasi.finalcase.review.domain.enums.ReviewReportStatus;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.entity.ReviewReport;
import com.onatsubasi.finalcase.review.domain.repository.ReviewReportRepository;
import com.onatsubasi.finalcase.review.domain.repository.ReviewRepository;
import com.onatsubasi.finalcase.review.infrastructure.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReportService {

    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reportRepository;
    private final ReviewEventPublisher eventPublisher;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewReportResponse reportReview(
            UserContext currentUser,
            UUID reviewId,
            ReportReviewRequest request
    ) {
        try {
            UUID userId = requireUserId(currentUser);

            MDC.put("eventName", "review.report.started");
            MDC.put("userId", userId.toString());

            if (reportRepository.existsByReviewIdAndReporterUserId(reviewId, userId)) {
                throw new BaseException(ReviewErrorCode.REVIEW_ALREADY_REPORTED);
            }

            Review review = reviewRepository.findByIdForUpdate(reviewId)
                    .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND));

            ReviewReport report = ReviewReport.create(
                    review,
                    userId,
                    request.reason(),
                    request.description()
            );

            ReviewReport saved = reportRepository.save(report);
            reviewRepository.save(review);
            eventPublisher.publishReviewReported(saved);

            MDC.put("eventName", "review.reported");
            log.info("Review reported, reviewId={}, reporterUserId={}, reason={}",
                    reviewId,
                    userId,
                    request.reason());

            return reviewMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("review.report.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewReportResponse> listReports(
            ReviewReportStatus status,
            Pageable pageable
    ) {
        Page<ReviewReport> page = status == null
                ? reportRepository.findAll(pageable)
                : reportRepository.findByStatus(status, pageable);

        return toPageResponse(page);
    }

    @Transactional
    public ReviewReportResponse resolveReport(
            UserContext admin,
            UUID reportId,
            ResolveReviewReportRequest request
    ) {
        UUID adminId = requireUserId(admin);

        ReviewReport report = reportRepository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_REPORT_NOT_FOUND));

        report.resolve(adminId, request.note());

        ReviewReport saved = reportRepository.save(report);
        eventPublisher.publishReviewReportResolved(saved);

        return reviewMapper.toResponse(saved);
    }

    @Transactional
    public ReviewReportResponse dismissReport(
            UserContext admin,
            UUID reportId,
            ResolveReviewReportRequest request
    ) {
        UUID adminId = requireUserId(admin);

        ReviewReport report = reportRepository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_REPORT_NOT_FOUND));

        report.dismiss(adminId, request.note());

        ReviewReport saved = reportRepository.save(report);
        eventPublisher.publishReviewReportResolved(saved);

        return reviewMapper.toResponse(saved);
    }

    private PageResponse<ReviewReportResponse> toPageResponse(Page<ReviewReport> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(reviewMapper::toResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private UUID requireUserId(UserContext currentUser) {
        if (currentUser == null || !currentUser.isAuthenticated() || currentUser.userId() == null) {
            throw new BaseException(ReviewErrorCode.INVALID_USER_ID);
        }

        return currentUser.userId();
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Review report operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
        MDC.remove("userId");
    }
}