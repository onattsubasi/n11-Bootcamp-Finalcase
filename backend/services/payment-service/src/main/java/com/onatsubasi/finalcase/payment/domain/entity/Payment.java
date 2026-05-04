package com.onatsubasi.finalcase.payment.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_checkout_id", columnList = "checkout_id"),
                @Index(name = "idx_payments_order_id", columnList = "order_id", unique = true),
                @Index(name = "idx_payments_user_id", columnList = "user_id"),
                @Index(name = "idx_payments_status", columnList = "status"),
                @Index(name = "idx_payments_provider", columnList = "provider")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "checkout_id", nullable = false)
    private UUID checkoutId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "order_number", nullable = false, length = 80)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 40)
    private PaymentProviderCode provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 50)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "refunded_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundedAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "provider_payment_id", length = 150)
    private String providerPaymentId;

    @Column(name = "provider_transaction_id", length = 150)
    private String providerTransactionId;

    @Column(name = "provider_conversation_id", length = 150)
    private String providerConversationId;

    @Column(name = "provider_status", length = 100)
    private String providerStatus;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentAttempt> attempts = new ArrayList<>();

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public Payment(
            UUID checkoutId,
            UUID orderId,
            String orderNumber,
            UUID userId,
            PaymentProviderCode provider,
            PaymentMethod method,
            BigDecimal amount,
            BigDecimal paidAmount,
            String currency
    ) {
        validateUuid(checkoutId, "Checkout id is required");
        validateUuid(orderId, "Order id is required");
        validateRequired(orderNumber, "Order number is required");
        validateUuid(userId, "User id is required");

        if (provider == null) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, "Payment provider is required");
        }

        if (method == null) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, "Payment method is required");
        }

        validateMoney(amount, "Payment amount must be greater than zero");
        validateMoney(paidAmount, "Paid amount must be greater than zero");
        validateCurrency(currency);

        this.checkoutId = checkoutId;
        this.orderId = orderId;
        this.orderNumber = orderNumber.trim();
        this.userId = userId;
        this.provider = provider;
        this.method = method;
        this.status = PaymentStatus.INITIATED;
        this.amount = amount;
        this.paidAmount = paidAmount;
        this.refundedAmount = BigDecimal.ZERO;
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
    }

    public void addAttempt(PaymentAttempt attempt) {
        if (attempt == null) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, "Payment attempt is required");
        }

        attempts.add(attempt);
        attempt.assignTo(this);
    }

    public void markWaitingProviderAction() {
        if (status == PaymentStatus.SUCCEEDED) {
            throw new BaseException(PaymentErrorCode.PAYMENT_ALREADY_SUCCEEDED);
        }

        this.status = PaymentStatus.WAITING_PROVIDER_ACTION;
    }

    public void markSucceeded(
            String providerPaymentId,
            String providerTransactionId,
            String providerConversationId,
            String providerStatus
    ) {
        if (status == PaymentStatus.SUCCEEDED) {
            return;
        }

        if (status == PaymentStatus.CANCELLED
                || status == PaymentStatus.REFUNDED
                || status == PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BaseException(PaymentErrorCode.PAYMENT_ALREADY_FINALIZED);
        }

        this.status = PaymentStatus.SUCCEEDED;
        this.providerPaymentId = normalize(providerPaymentId);
        this.providerTransactionId = normalize(providerTransactionId);
        this.providerConversationId = normalize(providerConversationId);
        this.providerStatus = normalize(providerStatus);
        this.failureReason = null;
        this.completedAt = Instant.now();
    }

    public void markFailed(
            String providerPaymentId,
            String providerTransactionId,
            String providerConversationId,
            String providerStatus,
            String failureReason
    ) {
        if (status == PaymentStatus.FAILED) {
            return;
        }

        if (status == PaymentStatus.SUCCEEDED) {
            throw new BaseException(PaymentErrorCode.PAYMENT_ALREADY_SUCCEEDED);
        }

        this.status = PaymentStatus.FAILED;
        this.providerPaymentId = normalize(providerPaymentId);
        this.providerTransactionId = normalize(providerTransactionId);
        this.providerConversationId = normalize(providerConversationId);
        this.providerStatus = normalize(providerStatus);
        this.failureReason = normalize(failureReason);
        this.completedAt = Instant.now();
    }

    public void markCancelled(
            String providerStatus,
            String providerTransactionId
    ) {
        if (status == PaymentStatus.CANCELLED) {
            return;
        }

        if (status == PaymentStatus.REFUNDED
                || status == PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BaseException(PaymentErrorCode.PAYMENT_CANCEL_NOT_ALLOWED);
        }

        this.status = PaymentStatus.CANCELLED;
        this.providerStatus = normalize(providerStatus);
        this.providerTransactionId = normalize(providerTransactionId);
        this.completedAt = Instant.now();
    }

    public void applyRefund(BigDecimal refundAmount) {
        if (status != PaymentStatus.SUCCEEDED
                && status != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BaseException(PaymentErrorCode.PAYMENT_REFUND_NOT_ALLOWED);
        }

        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(PaymentErrorCode.PAYMENT_REFUND_AMOUNT_INVALID);
        }

        BigDecimal newRefundedAmount = refundedAmount.add(refundAmount);

        if (newRefundedAmount.compareTo(paidAmount) > 0) {
            throw new BaseException(PaymentErrorCode.PAYMENT_REFUND_AMOUNT_INVALID);
        }

        this.refundedAmount = newRefundedAmount;

        if (newRefundedAmount.compareTo(paidAmount) == 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    public List<PaymentAttempt> getAttempts() {
        return List.copyOf(attempts);
    }

    private void validateUuid(UUID value, String message) {
        if (value == null) {
            throw new BaseException(PaymentErrorCode.INVALID_PAYMENT_DATA, message);
        }
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
