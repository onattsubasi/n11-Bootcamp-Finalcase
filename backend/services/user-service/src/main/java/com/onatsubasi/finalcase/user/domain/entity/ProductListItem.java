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
        name = "product_list_items",
        indexes = {
                @Index(name = "idx_product_list_items_list_id", columnList = "list_id"),
                @Index(name = "idx_product_list_items_product_id", columnList = "product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_list_items_list_product",
                        columnNames = {"list_id", "product_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false, updatable = false)
    private ProductList productList;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(length = 300)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private ProductListItem(
            ProductList productList,
            UUID productId,
            String note
    ) {
        validateProductList(productList);
        validateProductId(productId);

        this.productList = productList;
        this.productId = productId;
        this.note = normalize(note, 300);
        this.createdAt = Instant.now();
    }

    public static ProductListItem create(
            ProductList productList,
            UUID productId,
            String note
    ) {
        return new ProductListItem(productList, productId, note);
    }

    public void updateNote(String note) {
        this.note = normalize(note, 300);
    }

    private void validateProductList(ProductList productList) {
        if (productList == null) {
            throw new BaseException(UserErrorCode.PRODUCT_LIST_NOT_FOUND);
        }
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(UserErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return normalized.length() > maxLength
                ? normalized.substring(0, maxLength)
                : normalized;
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
