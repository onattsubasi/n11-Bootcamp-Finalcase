package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.domain.entity.FavoriteProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFavoriteProductJpaRepository extends JpaRepository<FavoriteProduct, UUID> {

    Optional<FavoriteProduct> findByUserIdAndProductId(UUID userId, UUID productId);

    List<FavoriteProduct> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
}
