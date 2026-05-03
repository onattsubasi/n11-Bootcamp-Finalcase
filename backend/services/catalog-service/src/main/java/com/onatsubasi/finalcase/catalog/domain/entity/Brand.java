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
        name = "brands",
        indexes = {
                @Index(name = "idx_brands_status", columnList = "status"),
                @Index(name = "idx_brands_name", columnList = "name")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_brands_slug", columnNames = "slug")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 140)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CatalogStatus status = CatalogStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private Brand(
            String name,
            String slug,
            String description,
            String logoUrl
    ) {
        validateName(name);
        validateSlug(slug);

        this.name = name.trim();
        this.slug = slug.trim();
        this.description = normalize(description);
        this.logoUrl = normalize(logoUrl);
        this.status = CatalogStatus.ACTIVE;
    }

    public static Brand create(
            String name,
            String slug,
            String description,
            String logoUrl
    ) {
        return new Brand(name, slug, description, logoUrl);
    }

    public void update(
            String name,
            String slug,
            String description,
            String logoUrl
    ) {
        ensureNotDeleted();
        validateName(name);
        validateSlug(slug);

        this.name = name.trim();
        this.slug = slug.trim();
        this.description = normalize(description);
        this.logoUrl = normalize(logoUrl);
    }

    public void activate() {
        ensureNotDeleted();
        this.status = CatalogStatus.ACTIVE;
    }

    public void suspend() {
        ensureNotDeleted();

        if (this.status != CatalogStatus.ACTIVE) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_BRAND_STATUS_TRANSITION,
                    "Only active brands can be suspended"
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
                    CatalogErrorCode.INVALID_BRAND_STATUS_TRANSITION,
                    "Deleted brand cannot be modified"
            );
        }
    }

    @PrePersist
    @PreUpdate
    private void normalizeBeforeSave() {
        this.name = normalizeRequired(this.name);
        this.slug = normalizeRequired(this.slug);
        this.description = normalize(this.description);
        this.logoUrl = normalize(this.logoUrl);

        if (this.status == null) {
            this.status = CatalogStatus.ACTIVE;
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_BRAND_DATA,
                    "Brand name is required"
            );
        }

        if (name.length() > 120) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_BRAND_DATA,
                    "Brand name cannot exceed 120 characters"
            );
        }
    }

    private void validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_BRAND_DATA,
                    "Brand slug is required"
            );
        }

        if (slug.length() > 140) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_BRAND_DATA,
                    "Brand slug cannot exceed 140 characters"
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