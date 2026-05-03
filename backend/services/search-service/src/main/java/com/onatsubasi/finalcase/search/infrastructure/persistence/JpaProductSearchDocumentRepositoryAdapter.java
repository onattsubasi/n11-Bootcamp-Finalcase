package com.onatsubasi.finalcase.search.infrastructure.persistence;

import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import com.onatsubasi.finalcase.search.domain.repository.ProductSearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaProductSearchDocumentRepositoryAdapter implements ProductSearchDocumentRepository {

    private final SpringDataProductSearchDocumentJpaRepository springDataRepository;

    @Override
    public ProductSearchDocument save(ProductSearchDocument document) {
        return springDataRepository.save(document);
    }

    @Override
    public Optional<ProductSearchDocument> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<ProductSearchDocument> findByProductId(UUID productId) {
        return springDataRepository.findByProductId(productId);
    }

    @Override
    public Optional<ProductSearchDocument> findByProductIdForUpdate(UUID productId) {
        return springDataRepository.findByProductIdForUpdate(productId);
    }

    @Override
    public List<ProductSearchDocument> findByProductIdsForUpdate(Collection<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        return springDataRepository.findByProductIdsForUpdate(productIds);
    }

    @Override
    public List<ProductSearchDocument> findByCategoryIdForUpdate(UUID categoryId) {
        return springDataRepository.findByCategoryIdForUpdate(categoryId);
    }

    @Override
    public List<ProductSearchDocument> findByBrandIdForUpdate(UUID brandId) {
        return springDataRepository.findByBrandIdForUpdate(brandId);
    }

    @Override
    public List<ProductSearchDocument> findByStatus(ProductSearchStatus status) {
        return springDataRepository.findByStatus(status);
    }

    @Override
    public void delete(ProductSearchDocument document) {
        springDataRepository.delete(document);
    }
}
