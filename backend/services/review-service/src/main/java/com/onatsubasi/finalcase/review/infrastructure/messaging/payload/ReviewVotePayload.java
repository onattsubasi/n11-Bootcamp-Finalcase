package com.onatsubasi.finalcase.review.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.review.domain.enums.ReviewVoteType;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.entity.ReviewVote;

import java.time.Instant;
import java.util.UUID;

public record ReviewVotePayload(
        UUID voteId,
        UUID reviewId,
        UUID productId,
        UUID userId,
        ReviewVoteType voteType,
        int helpfulCount,
        int unhelpfulCount,
        Instant createdAt,
        Instant updatedAt
) {

    public static ReviewVotePayload from(ReviewVote vote) {
        Review review = vote.getReview();

        return new ReviewVotePayload(
                vote.getId(),
                review.getId(),
                review.getProductId(),
                vote.getUserId(),
                vote.getVoteType(),
                review.getHelpfulCount(),
                review.getUnhelpfulCount(),
                vote.getCreatedAt(),
                vote.getUpdatedAt()
        );
    }

    public static ReviewVotePayload removed(Review review) {
        return new ReviewVotePayload(
                null,
                review.getId(),
                review.getProductId(),
                null,
                null,
                review.getHelpfulCount(),
                review.getUnhelpfulCount(),
                Instant.now(),
                Instant.now()
        );
    }
}
