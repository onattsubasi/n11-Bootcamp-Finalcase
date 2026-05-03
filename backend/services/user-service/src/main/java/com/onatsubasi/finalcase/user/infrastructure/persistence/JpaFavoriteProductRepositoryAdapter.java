package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.domain.entity.FavoriteProduct;
import com.onatsubasi.finalcase.user.domain.repository.FavoriteProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaFavoriteProductRepositoryAdapter implements FavoriteProductRepository {

    private final SpringDataFavoriteProductJpaRepository springDataRepository;

    @Override
    public FavoriteProduct save(FavoriteProduct favoriteProduct) {
        return springDataRepository.save(favoriteProduct);
    }

    @Override
    public Optional<FavoriteProduct> findByUserIdAndProductId(UUID userId, UUID productId) {
        return springDataRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<FavoriteProduct> findByUserId(UUID userId) {
        return springDataRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public boolean existsByUserIdAndProductId(UUID userId, UUID productId) {
        return springDataRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public void delete(FavoriteProduct favoriteProduct) {
        springDataRepository.delete(favoriteProduct);
    }
}
