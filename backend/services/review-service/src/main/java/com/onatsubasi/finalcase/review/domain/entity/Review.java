package com.onatsubasi.finalcase.review.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Getter
@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_product_id", columnList = "product_id"),
                @Index(name = "idx_reviews_user_id", columnList = "user_id"),
                @Index(name = "idx_reviews_order_id", columnList = "order_id"),
                @Index(name = "idx_reviews_status", columnList = "status"),
                @Index(name = "idx_reviews_visible", columnList = "visible"),
                @Index(name = "idx_reviews_rating", columnList = "rating"),
                @Index(name = "idx_reviews_created_at", columnList = "created_at"),
                @Index(name = "idx_reviews_deleted_at", columnList = "deleted_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MAX_TITLE_LENGTH = 150;
    public static final int MAX_COMMENT_LENGTH = 5000;
    public static final int MAX_IMAGES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "order_item_id", updatable = false)
    private UUID orderItemId;

    @Column(name = "order_number", nullable = false, updatable = false, length = 80)
    private String orderNumber;

    @Column(name = "delivered_at", updatable = false)
    private Instant deliveredAt;

    @Column(name = "author_display_name", nullable = false, length = 120)
    private String authorDisplayName;

    @Column(nullable = false)
    private int rating;

    @Column(length = MAX_TITLE_LENGTH)
    private String title;

    @Column(length = MAX_COMMENT_LENGTH)
    private String comment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReviewStatus status;

    @Column(nullable = false)
    private boolean visible;

    @Column(name = "verified_purchase", nullable = false)
    private boolean verifiedPurchase;

    @Column(name = "helpful_count", nullable = false)
    private int helpfulCount;

    @Column(name = "unhelpful_count", nullable = false)
    private int unhelpfulCount;

    @Column(name = "report_count", nullable = false)
    private int reportCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "moderation_metadata", columnDefinition = "jsonb")
    private Map<String, Object> moderationMetadata = new LinkedHashMap<>();

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "hidden_at")
    private Instant hiddenAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "last_moderated_by")
    private UUID lastModeratedBy;

    @Column(name = "last_moderated_at")
    private Instant lastModeratedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private Review(
            UUID productId,
            UUID userId,
            UUID orderId,
            UUID orderItemId,
            String orderNumber,
            Instant deliveredAt,
            String authorDisplayName,
            int rating,
            String title,
            String comment,
            List<Map<String, Object>> images,
            boolean autoApprove
    ) {
        validateProductId(productId);
        validateUserId(userId);
        validateOrderId(orderId);
        validateRequired(orderNumber, "Order number is required");
        validateRequired(authorDisplayName, "Author display name is required");
        validateRating(rating);
        validateImages(images);

        this.productId = productId;
        this.userId = userId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.orderNumber = normalize(orderNumber, 80);
        this.deliveredAt = deliveredAt;
        this.authorDisplayName = normalize(authorDisplayName, 120);
        this.rating = rating;
        this.title = normalize(title, MAX_TITLE_LENGTH);
        this.comment = normalize(comment, MAX_COMMENT_LENGTH);
        this.images = normalizeImages(images);
        this.verifiedPurchase = true;
        this.helpfulCount = 0;
        this.unhelpfulCount = 0;
        this.reportCount = 0;
        this.moderationMetadata = new LinkedHashMap<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;

        if (autoApprove) {
            this.status = ReviewStatus.APPROVED;
            this.visible = true;
            this.approvedAt = Instant.now();
        } else {
            this.status = ReviewStatus.PENDING_MODERATION;
            this.visible = false;
        }
    }

    public static Review createVerifiedPurchaseReview(
            UUID productId,
            UUID userId,
            UUID orderId,
            UUID orderItemId,
            String orderNumber,
            Instant deliveredAt,
            String authorDisplayName,
            int rating,
            String title,
            String comment,
            List<Map<String, Object>> images,
            boolean autoApprove
    ) {
        return new Review(
                productId,
                userId,
                orderId,
                orderItemId,
                orderNumber,
                deliveredAt,
                authorDisplayName,
                rating,
                title,
                comment,
                images,
                autoApprove
        );
    }

    public boolean updateContent(
            int rating,
            String title,
            String comment,
            List<Map<String, Object>> images,
            boolean autoApprove
    ) {
        ensureCustomerEditable();
        boolean wasPublic = contributesToRatingSummary();

        validateRating(rating);
        validateImages(images);

        this.rating = rating;
        this.title = normalize(title, MAX_TITLE_LENGTH);
        this.comment = normalize(comment, MAX_COMMENT_LENGTH);
        this.images = normalizeImages(images);

        if (autoApprove) {
            this.status = ReviewStatus.APPROVED;
            this.visible = true;

            if (this.approvedAt == null) {
                this.approvedAt = Instant.now();
            }

            this.rejectedAt = null;
            this.hiddenAt = null;
        } else {
            this.status = ReviewStatus.PENDING_MODERATION;
            this.visible = false;
            this.approvedAt = null;
            this.rejectedAt = null;
            this.hiddenAt = null;
        }

        touch();

        return wasPublic || contributesToRatingSummary();
    }

    public boolean approve(UUID moderatorUserId, String note) {
        ensureNotDeleted();

        boolean wasPublic = contributesToRatingSummary();

        this.status = ReviewStatus.APPROVED;
        this.visible = true;
        this.approvedAt = Instant.now();
        this.rejectedAt = null;
        this.hiddenAt = null;
        addModerationMetadata("APPROVED", moderatorUserId, note);
        touch();

        return wasPublic || contributesToRatingSummary();
    }

    public boolean reject(UUID moderatorUserId, String note) {
        ensureNotDeleted();

        boolean wasPublic = contributesToRatingSummary();

        this.status = ReviewStatus.REJECTED;
        this.visible = false;
        this.rejectedAt = Instant.now();
        this.hiddenAt = null;
        addModerationMetadata("REJECTED", moderatorUserId, note);
        touch();

        return wasPublic;
    }

    public boolean hide(UUID moderatorUserId, String note) {
        ensureNotDeleted();

        if (status != ReviewStatus.APPROVED && status != ReviewStatus.HIDDEN) {
            throw new BaseException(ReviewErrorCode.REVIEW_INVALID_STATUS);
        }

        boolean wasPublic = contributesToRatingSummary();

        this.status = ReviewStatus.HIDDEN;
        this.visible = false;
        this.hiddenAt = Instant.now();
        addModerationMetadata("HIDDEN", moderatorUserId, note);
        touch();

        return wasPublic;
    }

    public boolean restoreHidden(UUID moderatorUserId, String note) {
        ensureNotDeleted();

        if (status != ReviewStatus.HIDDEN) {
            throw new BaseException(ReviewErrorCode.REVIEW_INVALID_STATUS);
        }

        boolean wasPublic = contributesToRatingSummary();

        this.status = ReviewStatus.APPROVED;
        this.visible = true;
        this.hiddenAt = null;

        if (this.approvedAt == null) {
            this.approvedAt = Instant.now();
        }

        addModerationMetadata("RESTORED", moderatorUserId, note);
        touch();

        return wasPublic || contributesToRatingSummary();
    }

    public boolean softDeleteByCustomer(UUID currentUserId) {
        assertOwnedBy(currentUserId);
        return softDeleteInternal(null, "CUSTOMER_DELETED", null);
    }

    public boolean softDeleteByAdmin(UUID moderatorUserId, String note) {
        return softDeleteInternal(moderatorUserId, "ADMIN_DELETED", note);
    }

    public void assertOwnedBy(UUID currentUserId) {
        validateUserId(currentUserId);

        if (!this.userId.equals(currentUserId)) {
            throw new BaseException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }
    }

    public boolean contributesToRatingSummary() {
        return this.status == ReviewStatus.APPROVED
                && this.visible
                && this.deletedAt == null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null || this.status == ReviewStatus.DELETED;
    }

    public boolean hasImages() {
        return this.images != null && !this.images.isEmpty();
    }

    public void incrementHelpful() {
        this.helpfulCount++;
        touch();
    }

    public void decrementHelpful() {
        this.helpfulCount = Math.max(0, this.helpfulCount - 1);
        touch();
    }

    public void incrementUnhelpful() {
        this.unhelpfulCount++;
        touch();
    }

    public void decrementUnhelpful() {
        this.unhelpfulCount = Math.max(0, this.unhelpfulCount - 1);
        touch();
    }

    public void incrementReportCount() {
        this.reportCount++;
        touch();
    }

    public List<Map<String, Object>> getImages() {
        return images == null ? List.of() : List.copyOf(images);
    }

    public Map<String, Object> getModerationMetadata() {
        return moderationMetadata == null ? Map.of() : Map.copyOf(moderationMetadata);
    }

    private boolean softDeleteInternal(UUID moderatorUserId, String action, String note) {
        if (isDeleted()) {
            return false;
        }

        boolean wasPublic = contributesToRatingSummary();

        this.status = ReviewStatus.DELETED;
        this.visible = false;
        this.deletedAt = Instant.now();

        if (moderatorUserId != null) {
            addModerationMetadata(action, moderatorUserId, note);
        }

        touch();

        return wasPublic;
    }

    private void ensureCustomerEditable() {
        ensureNotDeleted();

        if (status == ReviewStatus.REJECTED) {
            throw new BaseException(ReviewErrorCode.REVIEW_INVALID_STATUS);
        }
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }
    }

    private void addModerationMetadata(String action, UUID moderatorUserId, String note) {
        if (moderationMetadata == null) {
            moderationMetadata = new LinkedHashMap<>();
        }

        this.lastModeratedBy = moderatorUserId;
        this.lastModeratedAt = Instant.now();

        Map<String, Object> actionEntry = new LinkedHashMap<>();
        actionEntry.put("action", action);
        actionEntry.put("moderatorUserId", moderatorUserId == null ? null : moderatorUserId.toString());
        actionEntry.put("note", normalize(note, 500));
        actionEntry.put("at", this.lastModeratedAt.toString());

        moderationMetadata.put("lastAction", actionEntry);
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(ReviewErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new BaseException(ReviewErrorCode.INVALID_USER_ID);
        }
    }

    private void validateOrderId(UUID orderId) {
        if (orderId == null) {
            throw new BaseException(ReviewErrorCode.INVALID_ORDER_ID);
        }
    }

    private void validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BaseException(ReviewErrorCode.REVIEW_INVALID_RATING);
        }
    }

    private void validateImages(List<Map<String, Object>> images) {
        if (images == null) {
            return;
        }

        if (images.size() > MAX_IMAGES) {
            throw new BaseException(
                    ReviewErrorCode.INVALID_REVIEW_IMAGE,
                    "Review cannot contain more than " + MAX_IMAGES + " images"
            );
        }

        for (Map<String, Object> image : images) {
            if (image == null || image.get("url") == null || image.get("url").toString().isBlank()) {
                throw new BaseException(
                        ReviewErrorCode.INVALID_REVIEW_IMAGE,
                        "Review image url is required"
                );
            }
        }
    }

    private List<Map<String, Object>> normalizeImages(List<Map<String, Object>> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }

        return images.stream()
                .map(map -> {
                    Map<String, Object> newMap = new LinkedHashMap<>(map);
                    Object url = newMap.get("url");
                    if (url != null) {
                        newMap.put("url", url.toString().trim());
                    }
                    return newMap;
                })
                .toList();
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(ReviewErrorCode.REVIEW_INVALID_DATA, message);
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
        if (images == null) {
            images = new ArrayList<>();
        }

        if (moderationMetadata == null) {
            moderationMetadata = new LinkedHashMap<>();
        }

        if (status == null) {
            status = ReviewStatus.PENDING_MODERATION;
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