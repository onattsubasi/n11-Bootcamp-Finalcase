package com.onatsubasi.finalcase.review.domain.repository;

import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;

import java.util.Optional;
import java.util.UUID;

public interface ProductRatingSummaryRepository {

    ProductRatingSummary save(ProductRatingSummary summary);

    Optional<ProductRatingSummary> findById(UUID id);

    Optional<ProductRatingSummary> findByProductId(UUID productId);

    Optional<ProductRatingSummary> findByProductIdForUpdate(UUID productId);
}