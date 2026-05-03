package com.onatsubasi.finalcase.catalog.domain.entity;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.valueobject.*;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Getter
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_status", columnList = "status"),
                @Index(name = "idx_products_brand_id", columnList = "brand_id"),
                @Index(name = "idx_products_category_id", columnList = "category_id"),
                @Index(name = "idx_products_updated_at", columnList = "updated_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_products_sku", columnNames = "sku"),
                @UniqueConstraint(name = "uk_products_slug", columnNames = "slug")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String sku;

    @Column(nullable = false, length = 220)
    private String slug;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "amount",
                    column = @Column(name = "base_price_amount", nullable = false, precision = 19, scale = 2)
            ),
            @AttributeOverride(
                    name = "currency",
                    column = @Column(name = "base_price_currency", nullable = false, length = 3)
            )
    })
    private Money basePrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "id",
                    column = @Column(name = "brand_id", nullable = false)
            ),
            @AttributeOverride(
                    name = "name",
                    column = @Column(name = "brand_name", nullable = false, length = 120)
            ),
            @AttributeOverride(
                    name = "slug",
                    column = @Column(name = "brand_slug", nullable = false, length = 140)
            )
    })
    private BrandSnapshot brand;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "id",
                    column = @Column(name = "category_id", nullable = false)
            ),
            @AttributeOverride(
                    name = "name",
                    column = @Column(name = "category_name", nullable = false, length = 120)
            ),
            @AttributeOverride(
                    name = "slug",
                    column = @Column(name = "category_slug", nullable = false, length = 140)
            ),
            @AttributeOverride(
                    name = "path",
                    column = @Column(name = "category_path", nullable = false, length = 1000)
            ),
            @AttributeOverride(
                    name = "ancestors",
                    column = @Column(name = "category_ancestors", nullable = false, columnDefinition = "jsonb")
            )
    })
    private CategorySnapshot category;

    @Embedded
    private ProductOwnership ownership;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<ProductImage> images = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, String> attributes = new LinkedHashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProductStatus status = ProductStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private Product(
            String sku,
            String name,
            String slug,
            String description,
            Money basePrice,
            BrandSnapshot brand,
            CategorySnapshot category,
            ProductOwnership ownership,
            List<ProductImage> images,
            Map<String, String> attributes
    ) {
        validateSku(sku);
        validateName(name);
        validateSlug(slug);
        validatePrice(basePrice);
        validateBrand(brand);
        validateCategory(category);
        validateOwnership(ownership);

        this.sku = sku.trim();
        this.name = name.trim();
        this.slug = slug.trim();
        this.description = normalize(description);
        this.basePrice = basePrice;
        this.brand = brand;
        this.category = category;
        this.ownership = ownership;
        this.images = copyImages(images);
        this.attributes = copyAttributes(attributes);
        this.status = ProductStatus.DRAFT;
    }

    public static Product createDraft(
            String sku,
            String name,
            String slug,
            String description,
            Money basePrice,
            BrandSnapshot brand,
            CategorySnapshot category,
            ProductOwnership ownership,
            List<ProductImage> images,
            Map<String, String> attributes
    ) {
        return new Product(
                sku,
                name,
                slug,
                description,
                basePrice,
                brand,
                category,
                ownership,
                images,
                attributes
        );
    }

    public void updateDetails(
            String name,
            String slug,
            String description,
            Money basePrice,
            BrandSnapshot brand,
            CategorySnapshot category,
            ProductOwnership ownership,
            List<ProductImage> images,
            Map<String, String> attributes
    ) {
        ensureNotDeleted();

        validateName(name);
        validateSlug(slug);
        validatePrice(basePrice);
        validateBrand(brand);
        validateCategory(category);
        validateOwnership(ownership);

        this.name = name.trim();
        this.slug = slug.trim();
        this.description = normalize(description);
        this.basePrice = basePrice;
        this.brand = brand;
        this.category = category;
        this.ownership = ownership;
        this.images = copyImages(images);
        this.attributes = copyAttributes(attributes);
    }

    public void changeCategory(CategorySnapshot category) {
        ensureNotDeleted();
        validateCategory(category);
        this.category = category;
    }

    public void changeBrand(BrandSnapshot brand) {
        ensureNotDeleted();
        validateBrand(brand);
        this.brand = brand;
    }

    public void changePrice(Money basePrice) {
        ensureNotDeleted();
        validatePrice(basePrice);
        this.basePrice = basePrice;
    }

    public void replaceImages(List<ProductImage> images) {
        ensureNotDeleted();
        this.images = copyImages(images);
    }

    public void replaceAttributes(Map<String, String> attributes) {
        ensureNotDeleted();
        this.attributes = copyAttributes(attributes);
    }

    public void activate() {
        ensureNotDeleted();

        if (this.status == ProductStatus.REJECTED) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_STATUS_TRANSITION,
                    "Rejected product cannot be directly activated"
            );
        }

        validateReadyForActivation();
        this.status = ProductStatus.ACTIVE;
    }

    public void suspend() {
        ensureNotDeleted();

        if (this.status != ProductStatus.ACTIVE) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_STATUS_TRANSITION,
                    "Only active products can be suspended"
            );
        }

        this.status = ProductStatus.SUSPENDED;
    }

    public void moveToDraft() {
        ensureNotDeleted();

        if (this.status == ProductStatus.ACTIVE) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_STATUS_TRANSITION,
                    "Active product cannot be moved to draft directly"
            );
        }

        this.status = ProductStatus.DRAFT;
    }

    public void markPendingApproval() {
        ensureNotDeleted();
        validateReadyForActivation();
        this.status = ProductStatus.PENDING_APPROVAL;
    }

    public void reject() {
        ensureNotDeleted();

        if (this.status != ProductStatus.PENDING_APPROVAL) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_STATUS_TRANSITION,
                    "Only pending approval products can be rejected"
            );
        }

        this.status = ProductStatus.REJECTED;
    }

    public void softDelete() {
        ensureNotDeleted();
        this.status = ProductStatus.DELETED;
    }

    public boolean isActive() {
        return this.status == ProductStatus.ACTIVE;
    }

    public boolean isSellable() {
        return this.status == ProductStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == ProductStatus.DELETED;
    }

    public List<ProductImage> getImages() {
        return List.copyOf(images);
    }

    public Map<String, String> getAttributes() {
        return Map.copyOf(attributes);
    }

    private void validateReadyForActivation() {
        validateSku(this.sku);
        validateName(this.name);
        validateSlug(this.slug);
        validatePrice(this.basePrice);
        validateBrand(this.brand);
        validateCategory(this.category);
        validateOwnership(this.ownership);
    }

    private void ensureNotDeleted() {
        if (this.status == ProductStatus.DELETED) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_STATUS_TRANSITION,
                    "Deleted product cannot be modified"
            );
        }
    }

    @PrePersist
    @PreUpdate
    private void normalizeBeforeSave() {
        this.sku = normalizeRequired(this.sku);
        this.name = normalizeRequired(this.name);
        this.slug = normalizeRequired(this.slug);
        this.description = normalize(this.description);

        if (this.images == null) {
            this.images = new ArrayList<>();
        }

        if (this.attributes == null) {
            this.attributes = new LinkedHashMap<>();
        }

        if (this.status == null) {
            this.status = ProductStatus.DRAFT;
        }
    }

    private void validateSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product SKU is required"
            );
        }

        if (sku.length() > 120) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product SKU cannot exceed 120 characters"
            );
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product name is required"
            );
        }

        if (name.length() > 200) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product name cannot exceed 200 characters"
            );
        }
    }

    private void validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product slug is required"
            );
        }

        if (slug.length() > 220) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product slug cannot exceed 220 characters"
            );
        }
    }

    private void validatePrice(Money price) {
        if (price == null) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRICE,
                    "Product base price is required"
            );
        }
    }

    private void validateBrand(BrandSnapshot brand) {
        if (brand == null) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product brand is required"
            );
        }
    }

    private void validateCategory(CategorySnapshot category) {
        if (category == null) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product category is required"
            );
        }
    }

    private void validateOwnership(ProductOwnership ownership) {
        if (ownership == null) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product ownership is required"
            );
        }
    }

    private List<ProductImage> copyImages(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(images);
    }

    private Map<String, String> copyAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<String, String> copied = new LinkedHashMap<>();

        attributes.forEach((key, value) -> {
            if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
                copied.put(key.trim(), value.trim());
            }
        });

        return copied;
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