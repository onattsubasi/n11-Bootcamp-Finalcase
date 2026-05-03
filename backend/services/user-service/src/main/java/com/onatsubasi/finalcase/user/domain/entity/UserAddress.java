package com.onatsubasi.finalcase.user.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.user.domain.enums.AddressType;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "user_addresses",
        indexes = {
                @Index(name = "idx_user_addresses_user_id", columnList = "user_id"),
                @Index(name = "idx_user_addresses_type", columnList = "type"),
                @Index(name = "idx_user_addresses_deleted", columnList = "deleted")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AddressType type;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "line1", nullable = false, length = 500)
    private String line1;

    @Column(name = "line2", length = 500)
    private String line2;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "default_shipping", nullable = false)
    private boolean defaultShipping;

    @Column(name = "default_billing", nullable = false)
    private boolean defaultBilling;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    private UserAddress(
            UUID userId,
            String title,
            AddressType type,
            String recipientName,
            String phoneNumber,
            String line1,
            String line2,
            String district,
            String city,
            String country,
            String postalCode,
            boolean defaultShipping,
            boolean defaultBilling
    ) {
        validateUserId(userId);
        validateType(type);
        validateRequired(title, "Address title is required");
        validateRequired(recipientName, "Recipient name is required");
        validateRequired(line1, "Address line1 is required");
        validateRequired(district, "District is required");
        validateRequired(city, "City is required");
        validateRequired(country, "Country is required");

        this.userId = userId;
        this.title = normalize(title, 100);
        this.type = type;
        this.recipientName = normalize(recipientName, 150);
        this.phoneNumber = normalize(phoneNumber, 30);
        this.line1 = normalize(line1, 500);
        this.line2 = normalize(line2, 500);
        this.district = normalize(district, 100);
        this.city = normalize(city, 100);
        this.country = normalize(country, 100);
        this.postalCode = normalize(postalCode, 20);
        this.defaultShipping = defaultShipping;
        this.defaultBilling = defaultBilling;
        this.deleted = false;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static UserAddress create(
            UUID userId,
            String title,
            AddressType type,
            String recipientName,
            String phoneNumber,
            String line1,
            String line2,
            String district,
            String city,
            String country,
            String postalCode,
            boolean defaultShipping,
            boolean defaultBilling
    ) {
        return new UserAddress(
                userId,
                title,
                type,
                recipientName,
                phoneNumber,
                line1,
                line2,
                district,
                city,
                country,
                postalCode,
                defaultShipping,
                defaultBilling
        );
    }

    public void update(
            String title,
            AddressType type,
            String recipientName,
            String phoneNumber,
            String line1,
            String line2,
            String district,
            String city,
            String country,
            String postalCode,
            boolean defaultShipping,
            boolean defaultBilling
    ) {
        ensureNotDeleted();
        validateType(type);
        validateRequired(title, "Address title is required");
        validateRequired(recipientName, "Recipient name is required");
        validateRequired(line1, "Address line1 is required");
        validateRequired(district, "District is required");
        validateRequired(city, "City is required");
        validateRequired(country, "Country is required");

        this.title = normalize(title, 100);
        this.type = type;
        this.recipientName = normalize(recipientName, 150);
        this.phoneNumber = normalize(phoneNumber, 30);
        this.line1 = normalize(line1, 500);
        this.line2 = normalize(line2, 500);
        this.district = normalize(district, 100);
        this.city = normalize(city, 100);
        this.country = normalize(country, 100);
        this.postalCode = normalize(postalCode, 20);
        this.defaultShipping = defaultShipping;
        this.defaultBilling = defaultBilling;
        touch();
    }

    public void markDefaultShipping() {
        ensureNotDeleted();

        this.defaultShipping = true;

        if (this.type == AddressType.BILLING) {
            this.type = AddressType.BOTH;
        }

        touch();
    }

    public void unmarkDefaultShipping() {
        if (this.defaultShipping) {
            this.defaultShipping = false;
            touch();
        }
    }

    public void markDefaultBilling() {
        ensureNotDeleted();

        this.defaultBilling = true;

        if (this.type == AddressType.SHIPPING) {
            this.type = AddressType.BOTH;
        }

        touch();
    }

    public void unmarkDefaultBilling() {
        if (this.defaultBilling) {
            this.defaultBilling = false;
            touch();
        }
    }

    public void softDelete() {
        if (this.deleted) {
            return;
        }

        this.deleted = true;
        this.defaultShipping = false;
        this.defaultBilling = false;
        touch();
    }

    public void assertOwnedBy(UUID currentUserId) {
        validateUserId(currentUserId);

        if (!this.userId.equals(currentUserId)) {
            throw new BaseException(UserErrorCode.ADDRESS_ACCESS_DENIED);
        }
    }

    public boolean canBeUsedAsShipping() {
        return !deleted && (type == AddressType.SHIPPING || type == AddressType.BOTH);
    }

    public boolean canBeUsedAsBilling() {
        return !deleted && (type == AddressType.BILLING || type == AddressType.BOTH);
    }

    private void ensureNotDeleted() {
        if (deleted) {
            throw new BaseException(UserErrorCode.ADDRESS_NOT_FOUND);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new BaseException(UserErrorCode.INVALID_USER_ID);
        }
    }

    private void validateType(AddressType type) {
        if (type == null) {
            throw new BaseException(UserErrorCode.INVALID_ADDRESS_DATA, "Address type is required");
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(UserErrorCode.INVALID_ADDRESS_DATA, message);
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
