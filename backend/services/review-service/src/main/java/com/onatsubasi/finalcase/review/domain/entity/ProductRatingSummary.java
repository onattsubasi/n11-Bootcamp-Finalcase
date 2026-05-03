package com.onatsubasi.finalcase.review.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "product_rating_summaries",
        indexes = {
                @Index(name = "idx_product_rating_summaries_product_id", columnList = "product_id", unique = true),
                @Index(name = "idx_product_rating_summaries_average_rating", columnList = "average_rating"),
                @Index(name = "idx_product_rating_summaries_review_count", columnList = "review_count")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_rating_summaries_product_id", columnNames = "product_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRatingSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(name = "review_count", nullable = false)
    private long reviewCount;

    @Column(name = "rating_1_count", nullable = false)
    private long rating1Count;

    @Column(name = "rating_2_count", nullable = false)
    private long rating2Count;

    @Column(name = "rating_3_count", nullable = false)
    private long rating3Count;

    @Column(name = "rating_4_count", nullable = false)
    private long rating4Count;

    @Column(name = "rating_5_count", nullable = false)
    private long rating5Count;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private ProductRatingSummary(UUID productId) {
        validateProductId(productId);

        this.productId = productId;
        this.averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.reviewCount = 0;
        this.rating1Count = 0;
        this.rating2Count = 0;
        this.rating3Count = 0;
        this.rating4Count = 0;
        this.rating5Count = 0;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static ProductRatingSummary empty(UUID productId) {
        return new ProductRatingSummary(productId);
    }

    public void updateCounts(
            long rating1Count,
            long rating2Count,
            long rating3Count,
            long rating4Count,
            long rating5Count
    ) {
        this.rating1Count = Math.max(0, rating1Count);
        this.rating2Count = Math.max(0, rating2Count);
        this.rating3Count = Math.max(0, rating3Count);
        this.rating4Count = Math.max(0, rating4Count);
        this.rating5Count = Math.max(0, rating5Count);

        this.reviewCount = this.rating1Count
                + this.rating2Count
                + this.rating3Count
                + this.rating4Count
                + this.rating5Count;

        if (reviewCount == 0) {
            this.averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            touch();
            return;
        }

        long weightedTotal =
                this.rating1Count
                        + this.rating2Count * 2
                        + this.rating3Count * 3
                        + this.rating4Count * 4
                        + this.rating5Count * 5;

        this.averageRating = BigDecimal.valueOf(weightedTotal)
                .divide(BigDecimal.valueOf(reviewCount), 2, RoundingMode.HALF_UP);

        touch();
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(ReviewErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (averageRating == null) {
            averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
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