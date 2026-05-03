package com.onatsubasi.finalcase.user.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.user.domain.entity.FavoriteProduct;

import java.time.Instant;
import java.util.UUID;

public record FavoriteProductPayload(
        UUID favoriteId,
        UUID userId,
        UUID productId,
        Instant createdAt
) {

    public static FavoriteProductPayload from(FavoriteProduct favoriteProduct) {
        return new FavoriteProductPayload(
                favoriteProduct.getId(),
                favoriteProduct.getUserId(),
                favoriteProduct.getProductId(),
                favoriteProduct.getCreatedAt()
        );
    }

    public static FavoriteProductPayload removed(UUID userId, UUID productId) {
        return new FavoriteProductPayload(
                null,
                userId,
                productId,
                Instant.now()
        );
    }
}
