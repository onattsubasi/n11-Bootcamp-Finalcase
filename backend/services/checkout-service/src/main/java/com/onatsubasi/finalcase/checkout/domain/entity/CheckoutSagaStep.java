package com.onatsubasi.finalcase.checkout.domain.entity;

import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepName;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepStatus;
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
        name = "checkout_saga_steps",
        indexes = {
                @Index(name = "idx_checkout_saga_steps_checkout_id", columnList = "checkout_id"),
                @Index(name = "idx_checkout_saga_steps_step_name", columnList = "step_name"),
                @Index(name = "idx_checkout_saga_steps_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckoutSagaStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_id", nullable = false)
    private CheckoutSession checkoutSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_name", nullable = false, length = 100)
    private CheckoutSagaStepName stepName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CheckoutSagaStepStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "external_reference_id", length = 150)
    private String externalReferenceId;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CheckoutSagaStep(CheckoutSagaStepName stepName) {
        if (stepName == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Saga step name is required");
        }

        this.stepName = stepName;
        this.status = CheckoutSagaStepStatus.STARTED;
        this.attemptCount = 1;
        this.startedAt = Instant.now();
        this.createdAt = this.startedAt;
        this.updatedAt = this.startedAt;
    }

    public static CheckoutSagaStep start(CheckoutSagaStepName stepName) {
        return new CheckoutSagaStep(stepName);
    }

    void assignTo(CheckoutSession checkoutSession) {
        if (checkoutSession == null) {
            throw new BaseException(
                    CheckoutErrorCode.INVALID_CHECKOUT_DATA,
                    "Saga step must belong to checkout session"
            );
        }

        this.checkoutSession = checkoutSession;
    }

    public void incrementAttempt() {
        this.attemptCount++;
        this.status = CheckoutSagaStepStatus.STARTED;
        this.startedAt = Instant.now();
        this.completedAt = null;
        this.failedAt = null;
        this.errorCode = null;
        this.errorMessage = null;
        touch();
    }

    public void succeed(String externalReferenceId) {
        this.status = CheckoutSagaStepStatus.SUCCEEDED;
        this.externalReferenceId = normalize(externalReferenceId, 150);
        this.completedAt = Instant.now();
        this.failedAt = null;
        this.errorCode = null;
        this.errorMessage = null;
        touch();
    }

    public void markCompleted() {
        succeed(this.externalReferenceId);
    }

    public void fail(String errorCode, String errorMessage) {
        this.status = CheckoutSagaStepStatus.FAILED;
        this.errorCode = normalize(errorCode, 100);
        this.errorMessage = normalize(errorMessage, 1000);
        this.failedAt = Instant.now();
        this.completedAt = null;
        touch();
    }

    public void markFailed(String errorMessage) {
        fail(null, errorMessage);
    }

    public void skip(String reason) {
        this.status = CheckoutSagaStepStatus.SKIPPED;
        this.errorMessage = normalize(reason, 1000);
        this.completedAt = Instant.now();
        this.failedAt = null;
        touch();
    }

    public void markSkipped(String reason) {
        skip(reason);
    }

    public void compensate(String externalReferenceId) {
        this.status = CheckoutSagaStepStatus.COMPENSATED;
        this.externalReferenceId = normalize(externalReferenceId, 150);
        this.completedAt = Instant.now();
        this.failedAt = null;
        touch();
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
            status = CheckoutSagaStepStatus.STARTED;
        }

        if (attemptCount <= 0) {
            attemptCount = 1;
        }

        if (startedAt == null) {
            startedAt = Instant.now();
        }

        if (createdAt == null) {
            createdAt = startedAt;
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
