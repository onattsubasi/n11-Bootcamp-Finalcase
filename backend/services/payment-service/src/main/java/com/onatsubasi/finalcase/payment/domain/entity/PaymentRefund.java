package com.onatsubasi.finalcase.payment.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.enums.RefundStatus;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(
        name = "payment_refunds",
        indexes = {
                @Index(name = "idx_payment_refunds_payment_id", columnList = "payment_id"),
                @Index(name = "idx_payment_refunds_idempotency_key", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_payment_refunds_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PaymentRefund {

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

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RefundStatus status;

    @Column(name = "provider_refund_id", length = 150)
    private String providerRefundId;

    @Column(name = "provider_status", length = 100)
    private String providerStatus;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public PaymentRefund(
            Payment payment,
            String idempotencyKey,
            String requestHash,
            BigDecimal amount,
            String currency
    ) {
        if (payment == null) {
            throw new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        validateRequired(idempotencyKey, "Idempotency key is required");
        validateRequired(requestHash, "Request hash is required");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(PaymentErrorCode.PAYMENT_REFUND_AMOUNT_INVALID);
        }

        validateCurrency(currency);

        this.payment = payment;
        this.idempotencyKey = idempotencyKey.trim();
        this.requestHash = requestHash.trim();
        this.amount = amount;
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
        this.status = RefundStatus.REQUESTED;
    }

    public void validateSameRequest(String requestHash) {
        if (requestHash == null || !this.requestHash.equals(requestHash)) {
            throw new BaseException(PaymentErrorCode.PAYMENT_IDEMPOTENCY_CONFLICT);
        }
    }

    public void markSucceeded(String providerRefundId, String providerStatus) {
        this.providerRefundId = normalize(providerRefundId);
        this.providerStatus = normalize(providerStatus);
        this.failureReason = null;
        this.status = RefundStatus.SUCCEEDED;
        this.completedAt = Instant.now();
    }

    public void markFailed(String providerStatus, String failureReason) {
        this.providerStatus = normalize(providerStatus);
        this.failureReason = normalize(failureReason);
        this.status = RefundStatus.FAILED;
        this.completedAt = Instant.now();
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, message);
        }
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.isBlank() || currency.trim().length() != 3) {
            throw new BaseException(PaymentErrorCode.PAYMENT_CURRENCY_INVALID);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
