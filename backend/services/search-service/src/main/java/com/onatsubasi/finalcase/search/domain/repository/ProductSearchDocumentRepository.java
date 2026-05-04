package com.onatsubasi.finalcase.search.domain.repository;

import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductSearchDocumentRepository {

    ProductSearchDocument save(ProductSearchDocument document);

    Optional<ProductSearchDocument> findById(UUID id);

    Optional<ProductSearchDocument> findByProductId(UUID productId);

    Optional<ProductSearchDocument> findByProductIdForUpdate(UUID productId);

    List<ProductSearchDocument> findByProductIdsForUpdate(Collection<UUID> productIds);

    List<ProductSearchDocument> findByCategoryIdForUpdate(UUID categoryId);

    List<ProductSearchDocument> findByBrandIdForUpdate(UUID brandId);

    List<ProductSearchDocument> findByStatus(ProductSearchStatus status);

    void delete(ProductSearchDocument document);
}