package com.onatsubasi.finalcase.payment.domain.model;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.enums.CancellationStatus;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payment_cancellations",
        indexes = {
                @Index(name = "idx_payment_cancellations_payment_id", columnList = "payment_id"),
                @Index(name = "idx_payment_cancellations_idempotency_key", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_payment_cancellations_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PaymentCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CancellationStatus status;

    @Column(name = "provider_cancel_id", length = 150)
    private String providerCancelId;

    @Column(name = "provider_status", length = 100)
    private String providerStatus;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public PaymentCancellation(
            Payment payment,
            String idempotencyKey,
            String requestHash
    ) {
        if (payment == null) {
            throw new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        validateRequired(idempotencyKey, "Idempotency key is required");
        validateRequired(requestHash, "Request hash is required");

        this.payment = payment;
        this.idempotencyKey = idempotencyKey.trim();
        this.requestHash = requestHash.trim();
        this.status = CancellationStatus.REQUESTED;
    }

    public void validateSameRequest(String requestHash) {
        if (requestHash == null || !this.requestHash.equals(requestHash)) {
            throw new BaseException(PaymentErrorCode.PAYMENT_IDEMPOTENCY_CONFLICT);
        }
    }

    public void markSucceeded(String providerCancelId, String providerStatus) {
        this.providerCancelId = normalize(providerCancelId);
        this.providerStatus = normalize(providerStatus);
        this.failureReason = null;
        this.status = CancellationStatus.SUCCEEDED;
        this.completedAt = Instant.now();
    }

    public void markFailed(String providerStatus, String failureReason) {
        this.providerStatus = normalize(providerStatus);
        this.failureReason = normalize(failureReason);
        this.status = CancellationStatus.FAILED;
        this.completedAt = Instant.now();
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, message);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
