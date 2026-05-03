package com.onatsubasi.finalcase.checkout.domain.entity;

import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutAddressType;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "checkout_addresses",
        indexes = {
                @Index(name = "idx_checkout_addresses_checkout_id", columnList = "checkout_id"),
                @Index(name = "idx_checkout_addresses_original_address_id", columnList = "original_address_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckoutAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_id", nullable = false)
    private CheckoutSession checkoutSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 30)
    private CheckoutAddressType addressType;

    @Column(name = "original_address_id")
    private UUID originalAddressId;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(name = "recipient_phone", length = 30)
    private String recipientPhone;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(length = 150)
    private String neighborhood;

    @Column(name = "address_line1", nullable = false, length = 500)
    private String addressLine1;

    @Column(name = "address_line2", length = 500)
    private String addressLine2;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private CheckoutAddress(
            CheckoutAddressType addressType,
            UUID originalAddressId,
            String recipientName,
            String recipientPhone,
            String country,
            String city,
            String district,
            String neighborhood,
            String addressLine1,
            String addressLine2,
            String postalCode
    ) {
        validateAddressType(addressType);
        validateRequired(recipientName, "Recipient name is required");
        validateRequired(country, "Country is required");
        validateRequired(city, "City is required");
        validateRequired(district, "District is required");
        validateRequired(addressLine1, "Address line1 is required");

        this.addressType = addressType;
        this.originalAddressId = originalAddressId;
        this.recipientName = normalize(recipientName, 150);
        this.recipientPhone = normalize(recipientPhone, 30);
        this.country = normalize(country, 100);
        this.city = normalize(city, 100);
        this.district = normalize(district, 100);
        this.neighborhood = normalize(neighborhood, 150);
        this.addressLine1 = normalize(addressLine1, 500);
        this.addressLine2 = normalize(addressLine2, 500);
        this.postalCode = normalize(postalCode, 30);
        this.createdAt = Instant.now();
    }

    public static CheckoutAddress create(
            CheckoutAddressType addressType,
            UUID originalAddressId,
            String recipientName,
            String recipientPhone,
            String country,
            String city,
            String district,
            String neighborhood,
            String addressLine1,
            String addressLine2,
            String postalCode
    ) {
        return new CheckoutAddress(
                addressType,
                originalAddressId,
                recipientName,
                recipientPhone,
                country,
                city,
                district,
                neighborhood,
                addressLine1,
                addressLine2,
                postalCode
        );
    }

    void assignTo(CheckoutSession checkoutSession) {
        if (checkoutSession == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Checkout address must belong to session");
        }

        this.checkoutSession = checkoutSession;
    }

    private void validateAddressType(CheckoutAddressType addressType) {
        if (addressType == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Address type is required");
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, message);
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

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
