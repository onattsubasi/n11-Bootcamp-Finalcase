package com.onatsubasi.finalcase.user.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
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
        name = "user_profiles",
        indexes = {
                @Index(name = "idx_user_profiles_email", columnList = "email"),
                @Index(name = "idx_user_profiles_status", columnList = "status"),
                @Index(name = "idx_user_profiles_updated_at", columnList = "updated_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    /**
     * Same UUID as Auth Service AuthAccount.id.
     */
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /**
     * Display/reference email copied from trusted gateway/auth context.
     * Login email ownership still belongs to Auth Service.
     */
    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(nullable = false, length = 10)
    private String language = "tr";

    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserProfileStatus status = UserProfileStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private UserProfile(UUID userId, String email, String defaultLanguage) {
        validateUserId(userId);
        validateEmail(email);

        this.userId = userId;
        this.email = normalizeEmail(email);
        this.language = normalizeLanguage(defaultLanguage);
        this.status = UserProfileStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static UserProfile createLazy(
            UUID userId,
            String email,
            String defaultLanguage
    ) {
        return new UserProfile(userId, email, defaultLanguage);
    }

    public void updateProfile(
            String firstName,
            String lastName,
            String phoneNumber,
            String avatarUrl,
            String language,
            boolean marketingOptIn
    ) {
        ensureActive();

        this.firstName = normalize(firstName, 100);
        this.lastName = normalize(lastName, 100);
        this.phoneNumber = normalize(phoneNumber, 30);
        this.avatarUrl = normalize(avatarUrl, 1000);
        this.language = normalizeLanguage(language);
        this.marketingOptIn = marketingOptIn;
        touch();
    }

    /**
     * Refreshes the email reference copied from Auth/Gateway context.
     *
     * Auth Service remains the source of truth for login email. User Service only keeps
     * this denormalized value for profile display and admin visibility. Returning a
     * boolean lets application services avoid unnecessary writes on read endpoints.
     */
    public boolean refreshEmailReference(String email) {
        validateEmail(email);

        String normalized = normalizeEmail(email);

        if (normalized.equals(this.email)) {
            return false;
        }

        this.email = normalized;
        touch();
        return true;
    }

    public void disable() {
        ensureNotDeleted();

        if (this.status == UserProfileStatus.DISABLED) {
            return;
        }

        this.status = UserProfileStatus.DISABLED;
        touch();
    }

    public void activate() {
        ensureNotDeleted();

        if (this.status == UserProfileStatus.ACTIVE) {
            return;
        }

        this.status = UserProfileStatus.ACTIVE;
        touch();
    }

    public void softDelete() {
        if (this.status == UserProfileStatus.DELETED) {
            return;
        }

        this.status = UserProfileStatus.DELETED;
        touch();
    }

    public boolean isActive() {
        return this.status == UserProfileStatus.ACTIVE;
    }

    private void ensureActive() {
        if (status == UserProfileStatus.DISABLED) {
            throw new BaseException(UserErrorCode.USER_PROFILE_DISABLED);
        }

        if (status == UserProfileStatus.DELETED) {
            throw new BaseException(UserErrorCode.USER_PROFILE_DELETED);
        }
    }

    private void ensureNotDeleted() {
        if (status == UserProfileStatus.DELETED) {
            throw new BaseException(UserErrorCode.USER_PROFILE_DELETED);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new BaseException(UserErrorCode.INVALID_USER_ID);
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new BaseException(UserErrorCode.INVALID_EMAIL);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "tr";
        }

        String normalized = language.trim().toLowerCase(Locale.ROOT);

        return normalized.length() > 10
                ? normalized.substring(0, 10)
                : normalized;
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
            status = UserProfileStatus.ACTIVE;
        }

        if (language == null || language.isBlank()) {
            language = "tr";
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        email = normalizeEmail(email);
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
        email = normalizeEmail(email);
    }
}