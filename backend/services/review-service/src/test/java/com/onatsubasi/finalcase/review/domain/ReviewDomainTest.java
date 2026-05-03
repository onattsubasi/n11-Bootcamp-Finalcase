package com.onatsubasi.finalcase.review.domain;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.testsupport.ReviewTestData;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewDomainTest {

    @Test
    void createsApprovedReviewWhenAutoApproveEnabled() {
        Review review = ReviewTestData.approvedReview();

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.APPROVED);
        assertThat(review.isVisible()).isTrue();
        assertThat(review.isVerifiedPurchase()).isTrue();
        assertThat(review.contributesToRatingSummary()).isTrue();
        assertThat(review.getImages()).hasSize(1);
    }

    @Test
    void createsPendingReviewWhenAutoApproveDisabled() {
        Review review = ReviewTestData.pendingReview();

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.PENDING_MODERATION);
        assertThat(review.isVisible()).isFalse();
        assertThat(review.contributesToRatingSummary()).isFalse();
    }

    @Test
    void updateApprovedReviewWithManualModerationMovesItBackToPending() {
        Review review = ReviewTestData.approvedReview();

        boolean shouldRecalculate = review.updateContent(4, "Updated", "Needs moderation", List.of(), false);

        assertThat(shouldRecalculate).isTrue();
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.PENDING_MODERATION);
        assertThat(review.isVisible()).isFalse();
        assertThat(review.contributesToRatingSummary()).isFalse();
    }

    @Test
    void hideAndRestoreReviewChangesRatingSummaryContribution() {
        Review review = ReviewTestData.approvedReview();

        boolean hideRecalculate = review.hide(ReviewTestData.ADMIN_ID, "bad language");
        boolean restoreRecalculate = review.restoreHidden(ReviewTestData.ADMIN_ID, "fixed");

        assertThat(hideRecalculate).isTrue();
        assertThat(restoreRecalculate).isTrue();
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.APPROVED);
        assertThat(review.isVisible()).isTrue();
    }

    @Test
    void rejectsInvalidRatingAndTooManyImages() {
        assertThatThrownBy(() -> Review.createVerifiedPurchaseReview(
                ReviewTestData.PRODUCT_ID,
                ReviewTestData.USER_ID,
                ReviewTestData.ORDER_ID,
                ReviewTestData.ORDER_ITEM_ID,
                "ORD-1",
                Instant.now(),
                "C***",
                6,
                "Invalid",
                "Invalid",
                List.of(),
                true
        )).isInstanceOf(BaseException.class);

        assertThatThrownBy(() -> Review.createVerifiedPurchaseReview(
                ReviewTestData.PRODUCT_ID,
                ReviewTestData.USER_ID,
                ReviewTestData.ORDER_ID,
                ReviewTestData.ORDER_ITEM_ID,
                "ORD-1",
                Instant.now(),
                "C***",
                5,
                "Too many images",
                "Invalid",
                List.of(
                        Map.of("url", "1"), Map.of("url", "2"), Map.of("url", "3"),
                        Map.of("url", "4"), Map.of("url", "5"), Map.of("url", "6")
                ),
                true
        )).isInstanceOf(BaseException.class);
    }

    @Test
    void customerCanOnlyDeleteOwnReview() {
        Review review = ReviewTestData.approvedReview();

        assertThatThrownBy(() -> review.softDeleteByCustomer(UUID.randomUUID()))
                .isInstanceOf(BaseException.class);

        boolean shouldRecalculate = review.softDeleteByCustomer(ReviewTestData.USER_ID);

        assertThat(shouldRecalculate).isTrue();
        assertThat(review.isDeleted()).isTrue();
        assertThat(review.contributesToRatingSummary()).isFalse();
    }
}
