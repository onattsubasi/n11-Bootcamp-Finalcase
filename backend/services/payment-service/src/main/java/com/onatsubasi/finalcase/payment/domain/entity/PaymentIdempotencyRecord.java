package com.onatsubasi.finalcase.payment.domain.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_idempotency_records", indexes = {
        @Index(name = "idx_payment_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_payment_idempotency_payment_id", columnList = "payment_id"),
        @Index(name = "idx_payment_idempotency_attempt_id", columnList = "payment_attempt_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PaymentIdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "payment_attempt_id")
    private UUID paymentAttemptId;

    @Column(name = "http_status")
    private Integer httpStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    private Map<String, Object> responsePayload = new HashMap<>();

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PaymentIdempotencyRecord(
            String idempotencyKey,
            String requestHash,
            Instant lockedUntil) {
        validateRequired(idempotencyKey, "Idempotency key is required");
        validateRequired(requestHash, "Request hash is required");

        this.idempotencyKey = idempotencyKey.trim();
        this.requestHash = requestHash.trim();
        this.lockedUntil = lockedUntil;
    }

    public void validateSameRequest(String requestHash) {
        if (requestHash == null || !this.requestHash.equals(requestHash)) {
            throw new BaseException(PaymentErrorCode.PAYMENT_IDEMPOTENCY_CONFLICT);
        }
    }

    public boolean hasStoredResponse() {
        return httpStatus != null && responsePayload != null && !responsePayload.isEmpty();
    }

    public void attachPayment(UUID paymentId, UUID paymentAttemptId) {
        this.paymentId = paymentId;
        this.paymentAttemptId = paymentAttemptId;
    }

    public void storeResponse(
            int httpStatus,
            Map<String, Object> responsePayload) {
        this.httpStatus = httpStatus;
        this.responsePayload = responsePayload == null
                ? new HashMap<>()
                : new HashMap<>(responsePayload);
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, message);
        }
    }
}
