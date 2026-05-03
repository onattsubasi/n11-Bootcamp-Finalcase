package com.onatsubasi.finalcase.review.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.review.application.dto.internal.VerifiedPurchaseResult;
import com.onatsubasi.finalcase.review.application.dto.request.CreateReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.UpdateReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.response.CustomerReviewResponse;
import com.onatsubasi.finalcase.review.application.port.ReviewEventPublisher;
import com.onatsubasi.finalcase.review.application.port.ReviewOrderGateway;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.repository.ReviewRepository;
import com.onatsubasi.finalcase.review.infrastructure.config.ReviewModerationProperties;
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
public class ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final ReviewOrderGateway orderGateway;
    private final RatingSummaryService ratingSummaryService;
    private final ReviewEventPublisher eventPublisher;
    private final ReviewModerationProperties moderationProperties;
    private final ReviewMapper reviewMapper;

    @Transactional
    public CustomerReviewResponse createReview(
            UserContext currentUser,
            CreateReviewRequest request
    ) {
        try {
            UUID userId = requireUserId(currentUser);

            MDC.put("eventName", "review.create.started");
            MDC.put("userId", userId.toString());

            if (reviewRepository.existsActiveByUserIdAndProductId(userId, request.productId())) {
                throw new BaseException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
            }

            VerifiedPurchaseResult purchase = orderGateway.verifyDeliveredPurchase(
                    userId,
                    request.productId()
            );

            if (purchase == null || !purchase.verified()) {
                throw new BaseException(ReviewErrorCode.REVIEW_PURCHASE_NOT_VERIFIED);
            }

            Review review = Review.createVerifiedPurchaseReview(
                    request.productId(),
                    userId,
                    purchase.orderId(),
                    purchase.orderItemId(),
                    purchase.orderNumber(),
                    purchase.deliveredAt(),
                    authorDisplayName(currentUser),
                    request.rating(),
                    request.title(),
                    request.comment(),
                    reviewMapper.toImageMaps(request.images()),
                    moderationProperties.isAutoApprove()
            );

            Review saved = reviewRepository.save(review);

            eventPublisher.publishReviewSubmitted(saved);

            if (saved.contributesToRatingSummary()) {
                eventPublisher.publishReviewApproved(saved);
                ratingSummaryService.recalculateAndPublish(saved.getProductId());
            }

            MDC.put("eventName", "review.created");
            log.info(
                    "Review created, reviewId={}, productId={}, userId={}, status={}",
                    saved.getId(),
                    saved.getProductId(),
                    saved.getUserId(),
                    saved.getStatus()
            );

            return reviewMapper.toCustomerResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("review.create.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public CustomerReviewResponse updateReview(
            UserContext currentUser,
            UUID reviewId,
            UpdateReviewRequest request
    ) {
        try {
            UUID userId = requireUserId(currentUser);

            MDC.put("eventName", "review.update.started");
            MDC.put("userId", userId.toString());

            Review review = reviewRepository.findByIdForUpdate(reviewId)
                    .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND));

            review.assertOwnedBy(userId);

            boolean shouldRecalculate = review.updateContent(
                    request.rating(),
                    request.title(),
                    request.comment(),
                    reviewMapper.toImageMaps(request.images()),
                    moderationProperties.isAutoApprove()
            );

            Review saved = reviewRepository.save(review);
            eventPublisher.publishReviewUpdated(saved);

            if (saved.contributesToRatingSummary()) {
                eventPublisher.publishReviewApproved(saved);
            }

            if (shouldRecalculate) {
                ratingSummaryService.recalculateAndPublish(saved.getProductId());
            }

            MDC.put("eventName", "review.updated");
            log.info("Review updated, reviewId={}, productId={}, userId={}",
                    saved.getId(),
                    saved.getProductId(),
                    saved.getUserId());

            return reviewMapper.toCustomerResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("review.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public void deleteMyReview(
            UserContext currentUser,
            UUID reviewId
    ) {
        try {
            UUID userId = requireUserId(currentUser);

            MDC.put("eventName", "review.delete.started");
            MDC.put("userId", userId.toString());

            Review review = reviewRepository.findByIdForUpdate(reviewId)
                    .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND));

            boolean shouldRecalculate = review.softDeleteByCustomer(userId);

            Review saved = reviewRepository.save(review);
            eventPublisher.publishReviewDeleted(saved);

            if (shouldRecalculate) {
                ratingSummaryService.recalculateAndPublish(saved.getProductId());
            }

            MDC.put("eventName", "review.deleted");
            log.info("Review soft deleted by customer, reviewId={}, userId={}", saved.getId(), userId);
        } catch (BaseException ex) {
            logBusinessFailure("review.delete.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    private UUID requireUserId(UserContext currentUser) {
        if (currentUser == null || !currentUser.isAuthenticated() || currentUser.userId() == null) {
            throw new BaseException(ReviewErrorCode.INVALID_USER_ID);
        }

        return currentUser.userId();
    }

    private String authorDisplayName(UserContext currentUser) {
        return reviewMapper.maskedDisplayName(
                null, // firstName not provided by gateway
                null, // lastName not provided by gateway
                currentUser.userId()
        );
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Review command operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
        MDC.remove("userId");
    }
}