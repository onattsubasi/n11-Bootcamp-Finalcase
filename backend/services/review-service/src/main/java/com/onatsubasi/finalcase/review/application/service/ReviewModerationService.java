package com.onatsubasi.finalcase.review.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.review.application.dto.request.ApproveReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.HideReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.RejectReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.RestoreReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.response.AdminReviewDetailResponse;
import com.onatsubasi.finalcase.review.application.port.ReviewEventPublisher;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.repository.ReviewRepository;
import com.onatsubasi.finalcase.review.infrastructure.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewModerationService {

    private final ReviewRepository reviewRepository;
    private final RatingSummaryService ratingSummaryService;
    private final ReviewEventPublisher eventPublisher;
    private final ReviewMapper reviewMapper;

    @Transactional
    public AdminReviewDetailResponse approveReview(
            UserContext admin,
            UUID reviewId,
            ApproveReviewRequest request
    ) {
        UUID adminId = requireUserId(admin);
        Review review = getReviewForUpdate(reviewId);

        boolean shouldRecalculate = review.approve(adminId, request.note());

        Review saved = reviewRepository.save(review);
        eventPublisher.publishReviewApproved(saved);

        if (shouldRecalculate) {
            ratingSummaryService.recalculateAndPublish(saved.getProductId());
        }

        logModeration("review.approved", saved, adminId);

        return reviewMapper.toAdminResponse(saved);
    }

    @Transactional
    public AdminReviewDetailResponse rejectReview(
            UserContext admin,
            UUID reviewId,
            RejectReviewRequest request
    ) {
        UUID adminId = requireUserId(admin);
        Review review = getReviewForUpdate(reviewId);

        boolean shouldRecalculate = review.reject(adminId, request.note());

        Review saved = reviewRepository.save(review);
        eventPublisher.publishReviewRejected(saved);

        if (shouldRecalculate) {
            ratingSummaryService.recalculateAndPublish(saved.getProductId());
        }

        logModeration("review.rejected", saved, adminId);

        return reviewMapper.toAdminResponse(saved);
    }

    @Transactional
    public AdminReviewDetailResponse hideReview(
            UserContext admin,
            UUID reviewId,
            HideReviewRequest request
    ) {
        UUID adminId = requireUserId(admin);
        Review review = getReviewForUpdate(reviewId);

        boolean shouldRecalculate = review.hide(adminId, request.note());

        Review saved = reviewRepository.save(review);
        eventPublisher.publishReviewHidden(saved);

        if (shouldRecalculate) {
            ratingSummaryService.recalculateAndPublish(saved.getProductId());
        }

        logModeration("review.hidden", saved, adminId);

        return reviewMapper.toAdminResponse(saved);
    }

    @Transactional
    public AdminReviewDetailResponse restoreReview(
            UserContext admin,
            UUID reviewId,
            RestoreReviewRequest request
    ) {
        UUID adminId = requireUserId(admin);
        Review review = getReviewForUpdate(reviewId);

        boolean shouldRecalculate = review.restoreHidden(adminId, request.note());

        Review saved = reviewRepository.save(review);
        eventPublisher.publishReviewRestored(saved);

        if (shouldRecalculate) {
            ratingSummaryService.recalculateAndPublish(saved.getProductId());
        }

        logModeration("review.restored", saved, adminId);

        return reviewMapper.toAdminResponse(saved);
    }

    @Transactional
    public void deleteReviewAsAdmin(
            UserContext admin,
            UUID reviewId,
            String note
    ) {
        UUID adminId = requireUserId(admin);
        Review review = getReviewForUpdate(reviewId);

        boolean shouldRecalculate = review.softDeleteByAdmin(adminId, note);

        Review saved = reviewRepository.save(review);
        eventPublisher.publishReviewDeleted(saved);

        if (shouldRecalculate) {
            ratingSummaryService.recalculateAndPublish(saved.getProductId());
        }

        logModeration("review.admin_deleted", saved, adminId);
    }

    private Review getReviewForUpdate(UUID reviewId) {
        return reviewRepository.findByIdForUpdate(reviewId)
                .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private UUID requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated() || userContext.userId() == null) {
            throw new BaseException(ReviewErrorCode.INVALID_USER_ID);
        }

        return userContext.userId();
    }

    private void logModeration(String eventName, Review review, UUID adminId) {
        try {
            MDC.put("eventName", eventName);
            MDC.put("userId", adminId.toString());

            log.info(
                    "Review moderation action completed, eventName={}, reviewId={}, productId={}, adminId={}, status={}",
                    eventName,
                    review.getId(),
                    review.getProductId(),
                    adminId,
                    review.getStatus()
            );
        } finally {
            MDC.remove("eventName");
            MDC.remove("userId");
        }
    }
}