package com.onatsubasi.finalcase.review.infrastructure.persistence;

import com.onatsubasi.finalcase.review.domain.entity.ReviewVote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataReviewVoteJpaRepository extends JpaRepository<ReviewVote, UUID> {

    @EntityGraph(attributePaths = "review")
    Optional<ReviewVote> findByReviewIdAndUserId(UUID reviewId, UUID userId);
}