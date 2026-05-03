package com.onatsubasi.finalcase.search.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.enums.StockStatus;
import com.onatsubasi.finalcase.search.domain.exception.SearchErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Getter
@Entity
@Table(
        name = "product_search_documents",
        indexes = {
                @Index(name = "idx_product_search_documents_product_id", columnList = "product_id", unique = true),
                @Index(name = "idx_product_search_documents_slug", columnList = "slug"),
                @Index(name = "idx_product_search_documents_brand_id", columnList = "brand_id"),
                @Index(name = "idx_product_search_documents_category_id", columnList = "category_id"),
                @Index(name = "idx_product_search_documents_status_visible", columnList = "status, visible"),
                @Index(name = "idx_product_search_documents_stock_status", columnList = "stock_status"),
                @Index(name = "idx_product_search_documents_base_price", columnList = "base_price"),
                @Index(name = "idx_product_search_documents_discounted_price", columnList = "discounted_price"),
                @Index(name = "idx_product_search_documents_average_rating", columnList = "average_rating"),
                @Index(name = "idx_product_search_documents_source_updated_at", columnList = "source_updated_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_search_documents_product_id",
                        columnNames = "product_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSearchDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false, length = 120)
    private String sku;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(length = 3000)
    private String description;

    @Column(name = "brand_id")
    private UUID brandId;

    @Column(name = "brand_name", length = 150)
    private String brandName;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "category_name", length = 150)
    private String categoryName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_path", nullable = false, columnDefinition = "jsonb")
    private List<String> categoryPath = new ArrayList<>();

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "discounted_price", precision = 19, scale = 2)
    private BigDecimal discountedPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> attributes = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false, length = 30)
    private StockStatus stockStatus = StockStatus.UNKNOWN;

    @Column(name = "has_discount", nullable = false)
    private boolean hasDiscount;

    @Column(name = "has_active_promotion", nullable = false)
    private boolean hasActivePromotion;

    @Column(name = "promotion_badge", length = 200)
    private String promotionBadge;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(name = "review_count", nullable = false)
    private long reviewCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductSearchStatus status = ProductSearchStatus.INACTIVE;

    @Column(nullable = false)
    private boolean visible;

    @Column(name = "source_updated_at")
    private Instant sourceUpdatedAt;

    @Column(name = "stock_updated_at")
    private Instant stockUpdatedAt;

    @Column(name = "promotion_updated_at")
    private Instant promotionUpdatedAt;

    @Column(name = "rating_updated_at")
    private Instant ratingUpdatedAt;

    @Column(name = "indexed_at", nullable = false)
    private Instant indexedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private ProductSearchDocument(
            UUID productId,
            String sku,
            String slug,
            String name,
            String description,
            UUID brandId,
            String brandName,
            UUID categoryId,
            String categoryName,
            List<String> categoryPath,
            BigDecimal basePrice,
            String currency,
            String imageUrl,
            Map<String, Object> attributes,
            List<String> tags,
            ProductSearchStatus status,
            boolean visible,
            Instant sourceUpdatedAt
    ) {
        validateProductId(productId);
        validateRequired(sku, "SKU is required");
        validateRequired(slug, "Slug is required");
        validateRequired(name, "Product name is required");
        validatePrice(basePrice, "Base price cannot be negative");
        validateCurrency(currency);

        this.productId = productId;
        this.sku = normalize(sku, 120);
        this.slug = normalize(slug, 180);
        this.name = normalize(name, 250);
        this.description = normalize(description, 3000);
        this.brandId = brandId;
        this.brandName = normalize(brandName, 150);
        this.categoryId = categoryId;
        this.categoryName = normalize(categoryName, 150);
        this.categoryPath = normalizeStringList(categoryPath, 300);
        this.basePrice = money(basePrice);
        this.discountedPrice = null;
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
        this.imageUrl = normalize(imageUrl, 1000);
        this.attributes = normalizeMap(attributes);
        this.tags = normalizeStringList(tags, 80);
        this.availableQuantity = 0;
        this.stockStatus = StockStatus.UNKNOWN;
        this.hasDiscount = false;
        this.hasActivePromotion = false;
        this.promotionBadge = null;
        this.averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.reviewCount = 0;
        this.status = status == null ? ProductSearchStatus.INACTIVE : status;
        this.visible = visible && this.status == ProductSearchStatus.ACTIVE;
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.indexedAt = Instant.now();
        this.createdAt = this.indexedAt;
        this.updatedAt = this.indexedAt;
    }

    public static ProductSearchDocument createFromCatalogProjection(
            UUID productId,
            String sku,
            String slug,
            String name,
            String description,
            UUID brandId,
            String brandName,
            UUID categoryId,
            String categoryName,
            List<String> categoryPath,
            BigDecimal basePrice,
            String currency,
            String imageUrl,
            Map<String, Object> attributes,
            List<String> tags,
            ProductSearchStatus status,
            boolean visible,
            Instant sourceUpdatedAt
    ) {
        return new ProductSearchDocument(
                productId,
                sku,
                slug,
                name,
                description,
                brandId,
                brandName,
                categoryId,
                categoryName,
                categoryPath,
                basePrice,
                currency,
                imageUrl,
                attributes,
                tags,
                status,
                visible,
                sourceUpdatedAt
        );
    }

    public boolean updateCatalogProjection(
            String sku,
            String slug,
            String name,
            String description,
            UUID brandId,
            String brandName,
            UUID categoryId,
            String categoryName,
            List<String> categoryPath,
            BigDecimal basePrice,
            String currency,
            String imageUrl,
            Map<String, Object> attributes,
            List<String> tags,
            ProductSearchStatus status,
            boolean visible,
            Instant sourceUpdatedAt
    ) {
        if (isOlderSourceUpdate(sourceUpdatedAt)) {
            return false;
        }

        validateRequired(sku, "SKU is required");
        validateRequired(slug, "Slug is required");
        validateRequired(name, "Product name is required");
        validatePrice(basePrice, "Base price cannot be negative");
        validateCurrency(currency);

        this.sku = normalize(sku, 120);
        this.slug = normalize(slug, 180);
        this.name = normalize(name, 250);
        this.description = normalize(description, 3000);
        this.brandId = brandId;
        this.brandName = normalize(brandName, 150);
        this.categoryId = categoryId;
        this.categoryName = normalize(categoryName, 150);
        this.categoryPath = normalizeStringList(categoryPath, 300);
        this.basePrice = money(basePrice);
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
        this.imageUrl = normalize(imageUrl, 1000);
        this.attributes = normalizeMap(attributes);
        this.tags = normalizeStringList(tags, 80);
        this.status = status == null ? ProductSearchStatus.INACTIVE : status;
        this.visible = visible && this.status == ProductSearchStatus.ACTIVE;
        this.sourceUpdatedAt = sourceUpdatedAt;
        touchIndex();

        return true;
    }

    public boolean updateCategoryProjection(
            UUID categoryId,
            String categoryName,
            List<String> categoryPath,
            Instant sourceUpdatedAt
    ) {
        if (isOlderSourceUpdate(sourceUpdatedAt)) {
            return false;
        }

        this.categoryId = categoryId;
        this.categoryName = normalize(categoryName, 150);
        this.categoryPath = normalizeStringList(categoryPath, 300);
        this.sourceUpdatedAt = sourceUpdatedAt;
        touchIndex();

        return true;
    }

    public boolean updateBrandProjection(
            UUID brandId,
            String brandName,
            Instant sourceUpdatedAt
    ) {
        if (isOlderSourceUpdate(sourceUpdatedAt)) {
            return false;
        }

        this.brandId = brandId;
        this.brandName = normalize(brandName, 150);
        this.sourceUpdatedAt = sourceUpdatedAt;
        touchIndex();

        return true;
    }

    public boolean updateStockProjection(
            int availableQuantity,
            StockStatus stockStatus,
            Instant stockUpdatedAt
    ) {
        if (stockUpdatedAt != null
                && this.stockUpdatedAt != null
                && stockUpdatedAt.isBefore(this.stockUpdatedAt)) {
            return false;
        }

        if (availableQuantity < 0) {
            throw new BaseException(
                    SearchErrorCode.INVALID_SEARCH_DOCUMENT_DATA,
                    "Available quantity cannot be negative"
            );
        }

        this.availableQuantity = availableQuantity;
        this.stockStatus = stockStatus == null ? StockStatus.UNKNOWN : stockStatus;
        this.stockUpdatedAt = stockUpdatedAt;
        touchIndex();

        return true;
    }

    public boolean updatePromotionProjection(
            boolean hasActivePromotion,
            boolean hasDiscount,
            BigDecimal discountedPrice,
            String promotionBadge,
            Instant promotionUpdatedAt
    ) {
        if (promotionUpdatedAt != null
                && this.promotionUpdatedAt != null
                && promotionUpdatedAt.isBefore(this.promotionUpdatedAt)) {
            return false;
        }

        if (discountedPrice != null) {
            validatePrice(discountedPrice, "Discounted price cannot be negative");
        }

        this.hasActivePromotion = hasActivePromotion;
        this.hasDiscount = hasDiscount;
        this.discountedPrice = discountedPrice == null ? null : money(discountedPrice);
        this.promotionBadge = hasActivePromotion ? normalize(promotionBadge, 200) : null;
        this.promotionUpdatedAt = promotionUpdatedAt;
        touchIndex();

        return true;
    }

    public boolean clearPromotionProjection(Instant promotionUpdatedAt) {
        if (promotionUpdatedAt != null
                && this.promotionUpdatedAt != null
                && promotionUpdatedAt.isBefore(this.promotionUpdatedAt)) {
            return false;
        }

        this.hasActivePromotion = false;
        this.hasDiscount = false;
        this.discountedPrice = null;
        this.promotionBadge = null;
        this.promotionUpdatedAt = promotionUpdatedAt;
        touchIndex();

        return true;
    }

    public boolean updateRatingProjection(
            BigDecimal averageRating,
            long reviewCount,
            Instant ratingUpdatedAt
    ) {
        if (ratingUpdatedAt != null
                && this.ratingUpdatedAt != null
                && ratingUpdatedAt.isBefore(this.ratingUpdatedAt)) {
            return false;
        }

        if (averageRating != null
                && (averageRating.compareTo(BigDecimal.ZERO) < 0
                || averageRating.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new BaseException(
                    SearchErrorCode.INVALID_SEARCH_DOCUMENT_DATA,
                    "Average rating must be between 0 and 5"
            );
        }

        if (reviewCount < 0) {
            throw new BaseException(
                    SearchErrorCode.INVALID_SEARCH_DOCUMENT_DATA,
                    "Review count cannot be negative"
            );
        }

        this.averageRating = averageRating == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : averageRating.setScale(2, RoundingMode.HALF_UP);
        this.reviewCount = reviewCount;
        this.ratingUpdatedAt = ratingUpdatedAt;
        touchIndex();

        return true;
    }

    public boolean markDeleted(Instant sourceUpdatedAt) {
        if (isOlderSourceUpdate(sourceUpdatedAt)) {
            return false;
        }

        this.status = ProductSearchStatus.DELETED;
        this.visible = false;
        this.sourceUpdatedAt = sourceUpdatedAt;
        touchIndex();

        return true;
    }

    public boolean markInactive(Instant sourceUpdatedAt) {
        if (isOlderSourceUpdate(sourceUpdatedAt)) {
            return false;
        }

        this.status = ProductSearchStatus.INACTIVE;
        this.visible = false;
        this.sourceUpdatedAt = sourceUpdatedAt;
        touchIndex();

        return true;
    }

    public boolean isPubliclyVisible() {
        return status == ProductSearchStatus.ACTIVE && visible;
    }

    public BigDecimal effectivePrice() {
        if (discountedPrice != null && discountedPrice.compareTo(BigDecimal.ZERO) >= 0) {
            return discountedPrice;
        }

        return basePrice;
    }

    public Map<String, Object> getAttributes() {
        return attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public List<String> getTags() {
        return tags == null ? List.of() : List.copyOf(tags);
    }

    public List<String> getCategoryPath() {
        return categoryPath == null ? List.of() : List.copyOf(categoryPath);
    }

    private boolean isOlderSourceUpdate(Instant incomingSourceUpdatedAt) {
        return incomingSourceUpdatedAt != null
                && this.sourceUpdatedAt != null
                && incomingSourceUpdatedAt.isBefore(this.sourceUpdatedAt);
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(SearchErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(SearchErrorCode.INVALID_SEARCH_DOCUMENT_DATA, message);
        }
    }

    private void validatePrice(BigDecimal price, String message) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(SearchErrorCode.INVALID_SEARCH_DOCUMENT_DATA, message);
        }
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.isBlank() || currency.trim().length() != 3) {
            throw new BaseException(
                    SearchErrorCode.INVALID_SEARCH_DOCUMENT_DATA,
                    "Currency must be a 3-letter code"
            );
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
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

    private List<String> normalizeStringList(List<String> values, int maxLength) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.length() > maxLength ? value.substring(0, maxLength) : value)
                .distinct()
                .toList();
    }

    private Map<String, Object> normalizeMap(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> normalized = new LinkedHashMap<>();

        input.forEach((key, value) -> {
            if (key != null && !key.isBlank() && value != null) {
                normalized.put(key.trim(), value);
            }
        });

        return normalized;
    }

    private void touchIndex() {
        this.indexedAt = Instant.now();
        this.updatedAt = this.indexedAt;
    }

    @PrePersist
    protected void prePersist() {
        if (categoryPath == null) {
            categoryPath = new ArrayList<>();
        }

        if (attributes == null) {
            attributes = new LinkedHashMap<>();
        }

        if (tags == null) {
            tags = new ArrayList<>();
        }

        if (averageRating == null) {
            averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if (status == null) {
            status = ProductSearchStatus.INACTIVE;
        }

        if (stockStatus == null) {
            stockStatus = StockStatus.UNKNOWN;
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (indexedAt == null) {
            indexedAt = createdAt;
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        visible = visible && status == ProductSearchStatus.ACTIVE;
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
        visible = visible && status == ProductSearchStatus.ACTIVE;
    }
}