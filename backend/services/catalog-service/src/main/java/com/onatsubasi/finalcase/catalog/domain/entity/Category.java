package com.onatsubasi.finalcase.catalog.domain.entity;

import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_categories_parent_id", columnList = "parent_id"),
                @Index(name = "idx_categories_status", columnList = "status"),
                @Index(name = "idx_categories_level", columnList = "level")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_categories_slug", columnNames = "slug"),
                @UniqueConstraint(name = "uk_categories_path", columnNames = "path")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 140)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, length = 1000)
    private String path;

    @Column(nullable = false)
    private int level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CatalogStatus status = CatalogStatus.ACTIVE;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private Category(
            String name,
            String slug,
            String description,
            UUID parentId,
            String path,
            int level,
            int sortOrder
    ) {
        validateName(name);
        validateSlug(slug);
        validatePath(path);
        validateLevel(level);

        this.name = name.trim();
        this.slug = slug.trim();
        this.description = normalize(description);
        this.parentId = parentId;
        this.path = path.trim();
        this.level = level;
        this.status = CatalogStatus.ACTIVE;
        this.sortOrder = Math.max(sortOrder, 0);
    }

    public static Category createRoot(
            String name,
            String slug,
            String description,
            int sortOrder
    ) {
        return new Category(
                name,
                slug,
                description,
                null,
                slug,
                0,
                sortOrder
        );
    }

    public static Category createChild(
            String name,
            String slug,
            String description,
            UUID parentId,
            String parentPath,
            int parentLevel,
            int sortOrder
    ) {
        if (parentId == null) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Parent category id is required"
            );
        }

        if (parentPath == null || parentPath.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Parent category path is required"
            );
        }

        if (parentLevel < 0) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Parent category level cannot be negative"
            );
        }

        return new Category(
                name,
                slug,
                description,
                parentId,
                parentPath.trim() + "/" + slug.trim(),
                parentLevel + 1,
                sortOrder
        );
    }

    public void update(
            String name,
            String slug,
            String description,
            UUID parentId,
            String path,
            int level,
            int sortOrder
    ) {
        ensureNotDeleted();
        validateName(name);
        validateSlug(slug);
        validatePath(path);
        validateLevel(level);

        if (this.id != null && this.id.equals(parentId)) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category cannot be parent of itself"
            );
        }

        this.name = name.trim();
        this.slug = slug.trim();
        this.description = normalize(description);
        this.parentId = parentId;
        this.path = path.trim();
        this.level = level;
        this.sortOrder = Math.max(sortOrder, 0);
    }

    public void activate() {
        ensureNotDeleted();
        this.status = CatalogStatus.ACTIVE;
    }

    public void suspend() {
        ensureNotDeleted();

        if (this.status != CatalogStatus.ACTIVE) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_STATUS_TRANSITION,
                    "Only active categories can be suspended"
            );
        }

        this.status = CatalogStatus.SUSPENDED;
    }

    public void softDelete() {
        ensureNotDeleted();
        this.status = CatalogStatus.DELETED;
    }

    public boolean isActive() {
        return this.status == CatalogStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == CatalogStatus.DELETED;
    }

    private void ensureNotDeleted() {
        if (this.status == CatalogStatus.DELETED) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_STATUS_TRANSITION,
                    "Deleted category cannot be modified"
            );
        }
    }

    @PrePersist
    @PreUpdate
    private void normalizeBeforeSave() {
        this.name = normalizeRequired(this.name);
        this.slug = normalizeRequired(this.slug);
        this.description = normalize(this.description);
        this.path = normalizeRequired(this.path);
        this.sortOrder = Math.max(this.sortOrder, 0);

        if (this.status == null) {
            this.status = CatalogStatus.ACTIVE;
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category name is required"
            );
        }

        if (name.length() > 120) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category name cannot exceed 120 characters"
            );
        }
    }

    private void validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category slug is required"
            );
        }

        if (slug.length() > 140) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category slug cannot exceed 140 characters"
            );
        }
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category path is required"
            );
        }

        if (path.length() > 1000) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category path cannot exceed 1000 characters"
            );
        }
    }

    private void validateLevel(int level) {
        if (level < 0) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category level cannot be negative"
            );
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }
}