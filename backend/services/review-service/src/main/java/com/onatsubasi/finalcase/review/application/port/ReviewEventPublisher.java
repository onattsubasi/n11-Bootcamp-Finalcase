package com.onatsubasi.finalcase.review.application.port;

import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.entity.ReviewReport;
import com.onatsubasi.finalcase.review.domain.entity.ReviewVote;

public interface ReviewEventPublisher {

    void publishReviewSubmitted(Review review);

    void publishReviewApproved(Review review);

    void publishReviewRejected(Review review);

    void publishReviewHidden(Review review);

    void publishReviewRestored(Review review);

    void publishReviewDeleted(Review review);

    void publishReviewUpdated(Review review);

    void publishRatingSummaryUpdated(ProductRatingSummary summary);

    void publishReviewVoted(ReviewVote vote);

    void publishReviewVoteRemoved(Review review);

    void publishReviewReported(ReviewReport report);

    void publishReviewReportResolved(ReviewReport report);
}