package com.onatsubasi.finalcase.review.infrastructure.persistence;

import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductRatingSummaryJpaRepository
        extends JpaRepository<ProductRatingSummary, UUID> {

    Optional<ProductRatingSummary> findByProductId(UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select s
             from ProductRatingSummary s
            where s.productId = :productId
           """)
    Optional<ProductRatingSummary> findByProductIdForUpdate(@Param("productId") UUID productId);
}