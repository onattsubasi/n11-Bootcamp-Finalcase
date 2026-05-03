package com.onatsubasi.finalcase.review.infrastructure.persistence;

import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataReviewJpaRepository extends JpaRepository<Review, UUID> {

    Optional<Review> findByIdAndUserId(UUID reviewId, UUID userId);

    Page<Review> findByUserId(UUID userId, Pageable pageable);

    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r
             from Review r
            where r.id = :reviewId
           """)
    Optional<Review> findByIdForUpdate(@Param("reviewId") UUID reviewId);

    @Query("""
           select r
             from Review r
            where r.userId = :userId
              and r.productId = :productId
              and r.deletedAt is null
           """)
    Optional<Review> findActiveByUserIdAndProductId(
            @Param("userId") UUID userId,
            @Param("productId") UUID productId
    );

    @Query("""
           select case when count(r) > 0 then true else false end
             from Review r
            where r.userId = :userId
              and r.productId = :productId
              and r.deletedAt is null
           """)
    boolean existsActiveByUserIdAndProductId(
            @Param("userId") UUID userId,
            @Param("productId") UUID productId
    );
}