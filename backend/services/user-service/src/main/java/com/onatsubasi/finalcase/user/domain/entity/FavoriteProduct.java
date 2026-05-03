package com.onatsubasi.finalcase.user.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "favorite_products",
        indexes = {
                @Index(name = "idx_favorite_products_user_id", columnList = "user_id"),
                @Index(name = "idx_favorite_products_product_id", columnList = "product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_favorite_products_user_product",
                        columnNames = {"user_id", "product_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private FavoriteProduct(UUID userId, UUID productId) {
        validateUserId(userId);
        validateProductId(productId);

        this.userId = userId;
        this.productId = productId;
        this.createdAt = Instant.now();
    }

    public static FavoriteProduct create(UUID userId, UUID productId) {
        return new FavoriteProduct(userId, productId);
    }

    public void assertOwnedBy(UUID currentUserId) {
        validateUserId(currentUserId);

        if (!this.userId.equals(currentUserId)) {
            throw new BaseException(UserErrorCode.FAVORITE_NOT_FOUND);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new BaseException(UserErrorCode.INVALID_USER_ID);
        }
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(UserErrorCode.INVALID_PRODUCT_ID);
        }
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
