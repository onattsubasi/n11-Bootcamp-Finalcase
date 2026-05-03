package com.onatsubasi.finalcase.promotion.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionStatus;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "promotions",
        indexes = {
                @Index(name = "idx_promotions_status_dates", columnList = "status, starts_at, ends_at"),
                @Index(name = "idx_promotions_type_status", columnList = "type, status"),
                @Index(name = "idx_promotions_priority", columnList = "priority")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PromotionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PromotionStatus status = PromotionStatus.DRAFT;

    @Column(name = "coupon_required", nullable = false)
    private boolean couponRequired;

    @Column(nullable = false)
    private boolean stackable;

    @Column(nullable = false)
    private int priority;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> ruleConfig = new HashMap<>();

    @Column(name = "global_usage_limit")
    private Integer globalUsageLimit;

    @Column(name = "per_user_usage_limit")
    private Integer perUserUsageLimit;

    @Column(name = "reserved_usage_count", nullable = false)
    private int reservedUsageCount;

    @Column(name = "redeemed_usage_count", nullable = false)
    private int redeemedUsageCount;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private Promotion(
            String name,
            String description,
            PromotionType type,
            boolean couponRequired,
            boolean stackable,
            int priority,
            Map<String, Object> ruleConfig,
            Integer globalUsageLimit,
            Integer perUserUsageLimit,
            Instant startsAt,
            Instant endsAt
    ) {
        validateRequired(name, "Promotion name is required");
        validateType(type);
        validateRuleConfig(ruleConfig);
        validatePriority(priority);
        validateLimit(globalUsageLimit, "Global usage limit must be greater than zero");
        validateLimit(perUserUsageLimit, "Per-user usage limit must be greater than zero");
        validateDateRange(startsAt, endsAt);

        this.name = name.trim();
        this.description = normalize(description, 1000);
        this.type = type;
        this.couponRequired = couponRequired;
        this.stackable = stackable;
        this.priority = priority;
        this.ruleConfig = new HashMap<>(ruleConfig);
        this.globalUsageLimit = globalUsageLimit;
        this.perUserUsageLimit = perUserUsageLimit;
        this.status = PromotionStatus.DRAFT;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Promotion create(
            String name,
            String description,
            PromotionType type,
            boolean couponRequired,
            boolean stackable,
            int priority,
            Map<String, Object> ruleConfig,
            Integer globalUsageLimit,
            Integer perUserUsageLimit,
            Instant startsAt,
            Instant endsAt
    ) {
        return new Promotion(
                name,
                description,
                type,
                couponRequired,
                stackable,
                priority,
                ruleConfig,
                globalUsageLimit,
                perUserUsageLimit,
                startsAt,
                endsAt
        );
    }

    public void update(
            String name,
            String description,
            boolean couponRequired,
            boolean stackable,
            int priority,
            Map<String, Object> ruleConfig,
            Integer globalUsageLimit,
            Integer perUserUsageLimit,
            Instant startsAt,
            Instant endsAt
    ) {
        ensureEditable();
        validateRequired(name, "Promotion name is required");
        validateRuleConfig(ruleConfig);
        validatePriority(priority);
        validateLimit(globalUsageLimit, "Global usage limit must be greater than zero");
        validateLimit(perUserUsageLimit, "Per-user usage limit must be greater than zero");
        validateDateRange(startsAt, endsAt);

        if (globalUsageLimit != null && usedCount() > globalUsageLimit) {
            throw new BaseException(
                    PromotionErrorCode.PROMOTION_USAGE_LIMIT_EXCEEDED,
                    "Global usage limit cannot be lower than current reserved + redeemed count"
            );
        }

        this.name = name.trim();
        this.description = normalize(description, 1000);
        this.couponRequired = couponRequired;
        this.stackable = stackable;
        this.priority = priority;
        this.ruleConfig = new HashMap<>(ruleConfig);
        this.globalUsageLimit = globalUsageLimit;
        this.perUserUsageLimit = perUserUsageLimit;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        touch();
    }

    public void activate() {
        ensureNotDeleted();

        if (isExpiredAt(Instant.now())) {
            throw new BaseException(PromotionErrorCode.PROMOTION_EXPIRED);
        }

        this.status = PromotionStatus.ACTIVE;
        touch();
    }

    public void pause() {
        ensureNotDeleted();

        if (this.status == PromotionStatus.PAUSED) {
            return;
        }

        if (this.status != PromotionStatus.ACTIVE) {
            throw new BaseException(PromotionErrorCode.PROMOTION_NOT_ACTIVE);
        }

        this.status = PromotionStatus.PAUSED;
        touch();
    }

    public void expire() {
        if (this.status == PromotionStatus.EXPIRED) {
            return;
        }

        ensureNotDeleted();

        this.status = PromotionStatus.EXPIRED;
        touch();
    }

    public void softDelete() {
        if (this.status == PromotionStatus.DELETED) {
            return;
        }

        this.status = PromotionStatus.DELETED;
        touch();
    }

    public void validateApplicableAt(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;

        if (this.status != PromotionStatus.ACTIVE) {
            throw new BaseException(PromotionErrorCode.PROMOTION_NOT_ACTIVE);
        }

        if (this.startsAt != null && this.startsAt.isAfter(referenceTime)) {
            throw new BaseException(PromotionErrorCode.PROMOTION_NOT_ACTIVE);
        }

        if (this.endsAt != null && this.endsAt.isBefore(referenceTime)) {
            throw new BaseException(PromotionErrorCode.PROMOTION_EXPIRED);
        }
    }

    public void reserveUsage() {
        validateApplicableAt(Instant.now());

        if (this.globalUsageLimit != null && usedCount() + 1 > this.globalUsageLimit) {
            throw new BaseException(PromotionErrorCode.PROMOTION_USAGE_LIMIT_EXCEEDED);
        }

        this.reservedUsageCount++;
        touch();
    }

    public void redeemReservedUsage() {
        if (this.reservedUsageCount <= 0) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION,
                    "Promotion has no reserved usage to redeem"
            );
        }

        this.reservedUsageCount--;
        this.redeemedUsageCount++;
        touch();
    }

    public void releaseReservedUsage() {
        if (this.reservedUsageCount <= 0) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION,
                    "Promotion has no reserved usage to release"
            );
        }

        this.reservedUsageCount--;
        touch();
    }

    public boolean isActiveAt(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;

        return status == PromotionStatus.ACTIVE
                && (startsAt == null || !startsAt.isAfter(referenceTime))
                && (endsAt == null || !endsAt.isBefore(referenceTime));
    }

    public boolean isExpiredAt(Instant now) {
        Instant referenceTime = now == null ? Instant.now() : now;
        return endsAt != null && endsAt.isBefore(referenceTime);
    }

    public int usedCount() {
        return reservedUsageCount + redeemedUsageCount;
    }

    public Map<String, Object> getRuleConfig() {
        return Map.copyOf(ruleConfig);
    }

    private void ensureEditable() {
        if (status == PromotionStatus.DELETED || status == PromotionStatus.EXPIRED) {
            throw new BaseException(PromotionErrorCode.INVALID_PROMOTION_DATA);
        }
    }

    private void ensureNotDeleted() {
        if (status == PromotionStatus.DELETED) {
            throw new BaseException(PromotionErrorCode.INVALID_PROMOTION_DATA);
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(PromotionErrorCode.INVALID_PROMOTION_DATA, message);
        }
    }

    private void validateType(PromotionType type) {
        if (type == null) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_DATA,
                    "Promotion type is required"
            );
        }
    }

    private void validateRuleConfig(Map<String, Object> ruleConfig) {
        if (ruleConfig == null || ruleConfig.isEmpty()) {
            throw new BaseException(PromotionErrorCode.INVALID_PROMOTION_RULE_CONFIG);
        }
    }

    private void validatePriority(int priority) {
        if (priority < 0) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_DATA,
                    "Priority cannot be negative"
            );
        }
    }

    private void validateLimit(Integer limit, String message) {
        if (limit != null && limit <= 0) {
            throw new BaseException(PromotionErrorCode.INVALID_PROMOTION_DATA, message);
        }
    }

    private void validateDateRange(Instant startsAt, Instant endsAt) {
        if (startsAt == null) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_DATA,
                    "Promotion start date is required"
            );
        }

        if (endsAt != null && !endsAt.isAfter(startsAt)) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_DATA,
                    "Promotion end date must be after start date"
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
        if (status == null) {
            status = PromotionStatus.DRAFT;
        }

        if (ruleConfig == null) {
            ruleConfig = new HashMap<>();
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