package com.onatsubasi.finalcase.review.domain.repository;

import com.onatsubasi.finalcase.review.domain.entity.ReviewVote;

import java.util.Optional;
import java.util.UUID;

public interface ReviewVoteRepository {

    ReviewVote save(ReviewVote vote);

    Optional<ReviewVote> findByReviewIdAndUserId(UUID reviewId, UUID userId);

    void delete(ReviewVote vote);
}