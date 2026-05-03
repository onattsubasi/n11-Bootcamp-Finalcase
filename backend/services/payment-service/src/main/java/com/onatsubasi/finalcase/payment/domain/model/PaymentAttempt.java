package com.onatsubasi.finalcase.payment.domain.model;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentAttemptStatus;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "payment_attempts",
        indexes = {
                @Index(name = "idx_payment_attempts_payment_id", columnList = "payment_id"),
                @Index(name = "idx_payment_attempts_provider_token", columnList = "provider, provider_token", unique = true),
                @Index(name = "idx_payment_attempts_idempotency_key", columnList = "idempotency_key"),
                @Index(name = "idx_payment_attempts_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 40)
    private PaymentProviderCode provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 50)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentAttemptStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "provider_token", length = 250)
    private String providerToken;

    @Column(name = "provider_payment_id", length = 150)
    private String providerPaymentId;

    @Column(name = "provider_transaction_id", length = 150)
    private String providerTransactionId;

    @Column(name = "provider_conversation_id", length = 150)
    private String providerConversationId;

    @Column(name = "provider_status", length = 100)
    private String providerStatus;

    @Column(name = "payment_page_url", length = 1500)
    private String paymentPageUrl;

    @Column(name = "checkout_form_content", columnDefinition = "text")
    private String checkoutFormContent;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_response_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> providerResponseSnapshot = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public PaymentAttempt(
            int attemptNumber,
            String idempotencyKey,
            String requestHash,
            PaymentProviderCode provider,
            PaymentMethod method,
            BigDecimal amount,
            BigDecimal paidAmount,
            String currency
    ) {
        if (attemptNumber <= 0) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, "Attempt number must be positive");
        }

        validateRequired(idempotencyKey, "Idempotency key is required");
        validateRequired(requestHash, "Request hash is required");

        if (provider == null) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, "Payment provider is required");
        }

        if (method == null) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, "Payment method is required");
        }

        validateMoney(amount, "Payment amount must be greater than zero");
        validateMoney(paidAmount, "Paid amount must be greater than zero");
        validateCurrency(currency);

        this.attemptNumber = attemptNumber;
        this.idempotencyKey = idempotencyKey.trim();
        this.requestHash = requestHash.trim();
        this.provider = provider;
        this.method = method;
        this.status = PaymentAttemptStatus.CREATED;
        this.amount = amount;
        this.paidAmount = paidAmount;
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
    }

    void assignTo(Payment payment) {
        if (payment == null) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, "Payment attempt must belong to a payment");
        }

        this.payment = payment;
    }

    public void markInitialized(
            String providerToken,
            String providerConversationId,
            String paymentPageUrl,
            String checkoutFormContent,
            Map<String, Object> providerResponseSnapshot
    ) {
        this.providerToken = normalize(providerToken);
        this.providerConversationId = normalize(providerConversationId);
        this.paymentPageUrl = normalize(paymentPageUrl);
        this.checkoutFormContent = normalize(checkoutFormContent);
        this.providerResponseSnapshot = providerResponseSnapshot == null
                ? new HashMap<>()
                : new HashMap<>(providerResponseSnapshot);
        this.status = PaymentAttemptStatus.INITIALIZED;
    }

    public void markWaitingProviderAction() {
        this.status = PaymentAttemptStatus.WAITING_PROVIDER_ACTION;
    }

    public void markProviderProcessing() {
        this.status = PaymentAttemptStatus.PROVIDER_PROCESSING;
    }

    public void markSucceeded(
            String providerPaymentId,
            String providerTransactionId,
            String providerConversationId,
            String providerStatus,
            Map<String, Object> providerResponseSnapshot
    ) {
        if (status == PaymentAttemptStatus.SUCCEEDED) {
            return;
        }

        this.providerPaymentId = normalize(providerPaymentId);
        this.providerTransactionId = normalize(providerTransactionId);
        this.providerConversationId = normalize(providerConversationId);
        this.providerStatus = normalize(providerStatus);
        this.providerResponseSnapshot = providerResponseSnapshot == null
                ? this.providerResponseSnapshot
                : new HashMap<>(providerResponseSnapshot);
        this.failureReason = null;
        this.status = PaymentAttemptStatus.SUCCEEDED;
        this.completedAt = Instant.now();
    }

    public void markFailed(
            String providerPaymentId,
            String providerTransactionId,
            String providerConversationId,
            String providerStatus,
            String failureReason,
            Map<String, Object> providerResponseSnapshot
    ) {
        if (status == PaymentAttemptStatus.FAILED) {
            return;
        }

        this.providerPaymentId = normalize(providerPaymentId);
        this.providerTransactionId = normalize(providerTransactionId);
        this.providerConversationId = normalize(providerConversationId);
        this.providerStatus = normalize(providerStatus);
        this.failureReason = normalize(failureReason);
        this.providerResponseSnapshot = providerResponseSnapshot == null
                ? this.providerResponseSnapshot
                : new HashMap<>(providerResponseSnapshot);
        this.status = PaymentAttemptStatus.FAILED;
        this.completedAt = Instant.now();
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, message);
        }
    }

    private void validateMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(PaymentErrorCode.PAYMENT_AMOUNT_INVALID, message);
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