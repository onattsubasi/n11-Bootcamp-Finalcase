package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import com.onatsubasi.finalcase.user.domain.repository.ProductListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaProductListRepositoryAdapter implements ProductListRepository {

    private final SpringDataProductListJpaRepository springDataRepository;

    @Override
    public ProductList save(ProductList productList) {
        return springDataRepository.save(productList);
    }

    @Override
    public Optional<ProductList> findById(UUID listId) {
        return springDataRepository.findById(listId);
    }

    @Override
    public Optional<ProductList> findByIdAndUserIdAndDeletedFalse(UUID listId, UUID userId) {
        return springDataRepository.findByIdAndUserIdAndDeletedFalse(listId, userId);
    }

    @Override
    public List<ProductList> findByUserIdAndDeletedFalse(UUID userId) {
        return springDataRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
    }
}
