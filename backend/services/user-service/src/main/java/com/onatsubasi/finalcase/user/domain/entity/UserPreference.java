package com.onatsubasi.finalcase.user.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "user_preferences",
        indexes = {
                @Index(name = "idx_user_preferences_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 10)
    private String language = "tr";

    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    @Column(name = "marketing_email_enabled", nullable = false)
    private boolean marketingEmailEnabled;

    @Column(name = "notification_email_enabled", nullable = false)
    private boolean notificationEmailEnabled = true;

    @Column(name = "notification_in_app_enabled", nullable = false)
    private boolean notificationInAppEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private UserPreference(UUID userId) {
        if (userId == null) {
            throw new BaseException(UserErrorCode.INVALID_USER_ID);
        }

        this.userId = userId;
        this.language = "tr";
        this.currency = "TRY";
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static UserPreference createDefault(UUID userId) {
        return new UserPreference(userId);
    }

    public void update(
            String language,
            String currency,
            boolean marketingEmailEnabled,
            boolean notificationEmailEnabled,
            boolean notificationInAppEnabled
    ) {
        this.language = normalizeLanguage(language);
        this.currency = normalizeCurrency(currency);
        this.marketingEmailEnabled = marketingEmailEnabled;
        this.notificationEmailEnabled = notificationEmailEnabled;
        this.notificationInAppEnabled = notificationInAppEnabled;
        touch();
    }

    private String normalizeLanguage(String value) {
        if (value == null || value.isBlank()) {
            return "tr";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        return normalized.length() > 10
                ? normalized.substring(0, 10)
                : normalized;
    }

    private String normalizeCurrency(String value) {
        if (value == null || value.isBlank()) {
            return "TRY";
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        if (normalized.length() != 3) {
            throw new BaseException(
                    UserErrorCode.INVALID_PREFERENCE_DATA,
                    "Currency must be a 3-letter code"
            );
        }

        return normalized;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        language = normalizeLanguage(language);
        currency = normalizeCurrency(currency);
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
        language = normalizeLanguage(language);
        currency = normalizeCurrency(currency);
    }
}
