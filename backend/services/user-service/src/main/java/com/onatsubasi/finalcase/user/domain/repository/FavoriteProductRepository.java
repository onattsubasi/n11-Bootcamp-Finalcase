package com.onatsubasi.finalcase.user.domain.repository;

import com.onatsubasi.finalcase.user.domain.entity.FavoriteProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteProductRepository {

    FavoriteProduct save(FavoriteProduct favoriteProduct);

    Optional<FavoriteProduct> findByUserIdAndProductId(UUID userId, UUID productId);

    List<FavoriteProduct> findByUserId(UUID userId);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    void delete(FavoriteProduct favoriteProduct);
}
