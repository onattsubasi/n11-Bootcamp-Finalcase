package com.onatsubasi.finalcase.user.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.user.domain.enums.ProductListVisibility;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "product_lists",
        indexes = {
                @Index(name = "idx_product_lists_user_id", columnList = "user_id"),
                @Index(name = "idx_product_lists_deleted", columnList = "deleted")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProductListVisibility visibility = ProductListVisibility.PRIVATE;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(
            mappedBy = "productList",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ProductListItem> items = new ArrayList<>();

    private ProductList(
            UUID userId,
            String name,
            String description,
            ProductListVisibility visibility
    ) {
        validateUserId(userId);
        validateName(name);

        this.userId = userId;
        this.name = normalize(name, 120);
        this.description = normalize(description, 500);
        this.visibility = visibility == null ? ProductListVisibility.PRIVATE : visibility;
        this.deleted = false;
        this.items = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static ProductList create(
            UUID userId,
            String name,
            String description,
            ProductListVisibility visibility
    ) {
        return new ProductList(userId, name, description, visibility);
    }

    public void update(
            String name,
            String description,
            ProductListVisibility visibility
    ) {
        ensureNotDeleted();
        validateName(name);

        this.name = normalize(name, 120);
        this.description = normalize(description, 500);
        this.visibility = visibility == null ? ProductListVisibility.PRIVATE : visibility;
        touch();
    }

    /**
     * Adds a product to the list. If the product already exists, only its note is updated.
     *
     * @return true if a new list item was created, false if an existing item was updated.
     */
    public boolean addItem(UUID productId, String note) {
        ensureNotDeleted();
        validateProductId(productId);

        Optional<ProductListItem> existingItem = findItem(productId);

        if (existingItem.isPresent()) {
            existingItem.get().updateNote(note);
            touch();
            return false;
        }

        ProductListItem item = ProductListItem.create(this, productId, note);
        this.items.add(item);
        touch();
        return true;
    }

    /**
     * Removes a product from the list. Missing items are treated as a safe no-op.
     *
     * @return true if an item was removed.
     */
    public boolean removeItem(UUID productId) {
        ensureNotDeleted();
        validateProductId(productId);

        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));

        if (removed) {
            touch();
        }

        return removed;
    }

    public void softDelete() {
        if (this.deleted) {
            return;
        }

        this.deleted = true;
        this.items.clear();
        touch();
    }

    public void assertOwnedBy(UUID currentUserId) {
        validateUserId(currentUserId);

        if (!this.userId.equals(currentUserId)) {
            throw new BaseException(UserErrorCode.PRODUCT_LIST_NOT_FOUND);
        }
    }

    public List<ProductListItem> getItems() {
        return items == null ? List.of() : List.copyOf(items);
    }

    private Optional<ProductListItem> findItem(UUID productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    private void ensureNotDeleted() {
        if (deleted) {
            throw new BaseException(UserErrorCode.PRODUCT_LIST_NOT_FOUND);
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

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BaseException(
                    UserErrorCode.INVALID_PRODUCT_LIST_DATA,
                    "Product list name is required"
            );
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

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (visibility == null) {
            visibility = ProductListVisibility.PRIVATE;
        }

        if (items == null) {
            items = new ArrayList<>();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}
