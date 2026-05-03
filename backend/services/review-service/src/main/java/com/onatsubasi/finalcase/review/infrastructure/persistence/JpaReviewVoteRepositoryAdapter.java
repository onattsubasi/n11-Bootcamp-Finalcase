package com.onatsubasi.finalcase.review.infrastructure.persistence;

import com.onatsubasi.finalcase.review.domain.entity.ReviewVote;
import com.onatsubasi.finalcase.review.domain.repository.ReviewVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaReviewVoteRepositoryAdapter implements ReviewVoteRepository {

    private final SpringDataReviewVoteJpaRepository springDataRepository;

    @Override
    public ReviewVote save(ReviewVote vote) {
        return springDataRepository.save(vote);
    }

    @Override
    public Optional<ReviewVote> findByReviewIdAndUserId(UUID reviewId, UUID userId) {
        return springDataRepository.findByReviewIdAndUserId(reviewId, userId);
    }

    @Override
    public void delete(ReviewVote vote) {
        springDataRepository.delete(vote);
    }
}
