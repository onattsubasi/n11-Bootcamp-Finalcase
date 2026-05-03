package com.onatsubasi.finalcase.checkout.domain.entity;

import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "checkout_idempotency_records",
        indexes = {
                @Index(name = "idx_checkout_idempotency_key", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_checkout_idempotency_user_id", columnList = "user_id"),
                @Index(name = "idx_checkout_idempotency_checkout_id", columnList = "checkout_session_id"),
                @Index(name = "idx_checkout_idempotency_locked_until", columnList = "locked_until")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_checkout_idempotency_records_key",
                        columnNames = "idempotency_key"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckoutIdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @Column(name = "checkout_session_id")
    private UUID checkoutSessionId;

    @Column(name = "http_status")
    private Integer httpStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    private Map<String, Object> responsePayload = new LinkedHashMap<>();

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CheckoutIdempotencyRecord(
            String idempotencyKey,
            UUID userId,
            String requestHash,
            Instant lockedUntil
    ) {
        validateRequired(idempotencyKey, "Idempotency key is required");
        validateUuid(userId, "User id is required");
        validateRequired(requestHash, "Request hash is required");

        this.idempotencyKey = normalize(idempotencyKey, 120);
        this.userId = userId;
        this.requestHash = normalize(requestHash, 128);
        this.lockedUntil = lockedUntil;
        this.responsePayload = new LinkedHashMap<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static CheckoutIdempotencyRecord create(
            String idempotencyKey,
            UUID userId,
            String requestHash,
            Instant lockedUntil
    ) {
        return new CheckoutIdempotencyRecord(
                idempotencyKey,
                userId,
                requestHash,
                lockedUntil
        );
    }

    public void validateSameRequest(String requestHash) {
        if (requestHash == null || !this.requestHash.equals(requestHash)) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_IDEMPOTENCY_CONFLICT);
        }
    }

    public boolean hasStoredResponse() {
        return httpStatus != null && responsePayload != null && !responsePayload.isEmpty();
    }

    public void attachCheckout(UUID checkoutSessionId) {
        validateUuid(checkoutSessionId, "Checkout session id is required");

        this.checkoutSessionId = checkoutSessionId;
        touch();
    }

    public void storeResponse(
            int httpStatus,
            Map<String, Object> responsePayload
    ) {
        if (httpStatus < 100 || httpStatus > 599) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Invalid HTTP status");
        }

        this.httpStatus = httpStatus;
        this.responsePayload = responsePayload == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(responsePayload);
        this.lockedUntil = null;
        touch();
    }

    public boolean isLockedAt(Instant now) {
        return lockedUntil != null && now != null && now.isBefore(lockedUntil);
    }

    public void extendLock(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
        touch();
    }

    public void releaseLock() {
        this.lockedUntil = null;
        touch();
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, message);
        }
    }

    private void validateUuid(UUID value, String message) {
        if (value == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, message);
        }
    }

    private String normalize(String value, int maxLength) {
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
        if (responsePayload == null) {
            responsePayload = new LinkedHashMap<>();
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
