package com.onatsubasi.finalcase.review.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.review.application.dto.internal.VerifiedPurchaseResult;
import com.onatsubasi.finalcase.review.application.dto.request.CreateReviewRequest;
import com.onatsubasi.finalcase.review.application.port.ReviewEventPublisher;
import com.onatsubasi.finalcase.review.application.port.ReviewOrderGateway;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.repository.ReviewRepository;
import com.onatsubasi.finalcase.review.infrastructure.config.ReviewModerationProperties;
import com.onatsubasi.finalcase.review.infrastructure.mapper.ReviewMapper;
import com.onatsubasi.finalcase.review.testsupport.ReviewTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewCommandServiceTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    ReviewOrderGateway orderGateway;

    @Mock
    RatingSummaryService ratingSummaryService;

    @Mock
    ReviewEventPublisher eventPublisher;

    ReviewModerationProperties moderationProperties;
    ReviewMapper reviewMapper;
    ReviewCommandService service;

    @BeforeEach
    void setUp() {
        moderationProperties = new ReviewModerationProperties();
        reviewMapper = new ReviewMapper();
        service = new ReviewCommandService(
                reviewRepository,
                orderGateway,
                ratingSummaryService,
                eventPublisher,
                moderationProperties,
                reviewMapper
        );
    }

    @Test
    void createsReviewOnlyAfterDeliveredPurchaseVerification() {
        moderationProperties.setAutoApprove(true);
        CreateReviewRequest request = new CreateReviewRequest(
                ReviewTestData.PRODUCT_ID,
                5,
                "Great",
                "Good product",
                List.of()
        );

        when(reviewRepository.existsActiveByUserIdAndProductId(ReviewTestData.USER_ID, ReviewTestData.PRODUCT_ID))
                .thenReturn(false);
        when(orderGateway.verifyDeliveredPurchase(ReviewTestData.USER_ID, ReviewTestData.PRODUCT_ID))
                .thenReturn(new VerifiedPurchaseResult(
                        true,
                        ReviewTestData.ORDER_ID,
                        ReviewTestData.ORDER_ITEM_ID,
                        "ORD-20260503-000001",
                        Instant.parse("2026-05-03T10:00:00Z")
                ));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createReview(ReviewTestData.customer(), request);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review saved = reviewCaptor.getValue();

        assertThat(saved.getProductId()).isEqualTo(ReviewTestData.PRODUCT_ID);
        assertThat(saved.getUserId()).isEqualTo(ReviewTestData.USER_ID);
        assertThat(saved.contributesToRatingSummary()).isTrue();
        verify(eventPublisher).publishReviewSubmitted(saved);
        verify(ratingSummaryService).recalculateAndPublish(ReviewTestData.PRODUCT_ID);
    }

    @Test
    void rejectsDuplicateActiveReviewBeforeCallingOrderService() {
        CreateReviewRequest request = new CreateReviewRequest(
                ReviewTestData.PRODUCT_ID,
                5,
                null,
                null,
                List.of()
        );

        when(reviewRepository.existsActiveByUserIdAndProductId(ReviewTestData.USER_ID, ReviewTestData.PRODUCT_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createReview(ReviewTestData.customer(), request))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(ReviewErrorCode.REVIEW_ALREADY_EXISTS);

        verifyNoInteractions(orderGateway);
    }

    @Test
    void rejectsReviewWhenDeliveredPurchaseCannotBeVerified() {
        CreateReviewRequest request = new CreateReviewRequest(
                ReviewTestData.PRODUCT_ID,
                5,
                null,
                null,
                List.of()
        );

        when(reviewRepository.existsActiveByUserIdAndProductId(ReviewTestData.USER_ID, ReviewTestData.PRODUCT_ID))
                .thenReturn(false);
        when(orderGateway.verifyDeliveredPurchase(ReviewTestData.USER_ID, ReviewTestData.PRODUCT_ID))
                .thenReturn(new VerifiedPurchaseResult(false, null, null, null, null));

        assertThatThrownBy(() -> service.createReview(ReviewTestData.customer(), request))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(ReviewErrorCode.REVIEW_PURCHASE_NOT_VERIFIED);

        verify(reviewRepository, never()).save(any());
    }
}
