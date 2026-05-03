package com.onatsubasi.finalcase.checkout.domain.entity;

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
        name = "processed_payment_events",
        indexes = {
                @Index(name = "idx_processed_payment_events_event_id", columnList = "event_id", unique = true),
                @Index(name = "idx_processed_payment_events_event_type", columnList = "event_type"),
                @Index(name = "idx_processed_payment_events_payment_id", columnList = "payment_id"),
                @Index(name = "idx_processed_payment_events_checkout_id", columnList = "checkout_session_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processed_payment_events_event_id",
                        columnNames = "event_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedPaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true, length = 120)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "checkout_session_id")
    private UUID checkoutSessionId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public ProcessedPaymentEvent(
            String eventId,
            String eventType,
            UUID paymentId,
            UUID checkoutSessionId
    ) {
        validateRequired(eventId, "Event id is required");
        validateRequired(eventType, "Event type is required");

        this.eventId = normalize(eventId, 120);
        this.eventType = normalize(eventType, 120);
        this.paymentId = paymentId;
        this.checkoutSessionId = checkoutSessionId;
        this.processedAt = Instant.now();
    }

    public static ProcessedPaymentEvent create(
            String eventId,
            String eventType,
            UUID paymentId,
            UUID checkoutSessionId
    ) {
        return new ProcessedPaymentEvent(
                eventId,
                eventType,
                paymentId,
                checkoutSessionId
        );
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(CheckoutErrorCode.INVALID_PAYMENT_EVENT, message);
        }
    }

    private String normalize(String value, int maxLength) {
        String normalized = value.trim();

        return normalized.length() > maxLength
                ? normalized.substring(0, maxLength)
                : normalized;
    }

    @PrePersist
    protected void prePersist() {
        if (processedAt == null) {
            processedAt = Instant.now();
        }
    }
}