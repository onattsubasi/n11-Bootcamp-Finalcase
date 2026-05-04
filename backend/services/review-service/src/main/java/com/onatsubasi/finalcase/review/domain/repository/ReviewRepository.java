package com.onatsubasi.finalcase.review.domain.repository;

import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(UUID reviewId);

    Optional<Review> findByIdForUpdate(UUID reviewId);

    Optional<Review> findByIdAndUserId(UUID reviewId, UUID userId);

    Optional<Review> findActiveByUserIdAndProductId(UUID userId, UUID productId);

    boolean existsActiveByUserIdAndProductId(UUID userId, UUID productId);

    Page<Review> findPublicReviews(
            UUID productId,
            Integer rating,
            boolean withImagesOnly,
            Pageable pageable
    );

    Page<Review> findByUserId(UUID userId, Pageable pageable);

    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    Page<Review> findAll(Pageable pageable);

    RatingSummaryStats calculateSummaryStats(UUID productId);
}