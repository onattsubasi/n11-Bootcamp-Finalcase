package com.onatsubasi.finalcase.review.infrastructure.persistence;

import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;
import com.onatsubasi.finalcase.review.domain.repository.ProductRatingSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaProductRatingSummaryRepositoryAdapter implements ProductRatingSummaryRepository {

    private final SpringDataProductRatingSummaryJpaRepository springDataRepository;

    @Override
    public ProductRatingSummary save(ProductRatingSummary summary) {
        return springDataRepository.save(summary);
    }

    @Override
    public Optional<ProductRatingSummary> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<ProductRatingSummary> findByProductId(UUID productId) {
        return springDataRepository.findByProductId(productId);
    }

    @Override
    public Optional<ProductRatingSummary> findByProductIdForUpdate(UUID productId) {
        return springDataRepository.findByProductIdForUpdate(productId);
    }
}
