package com.onatsubasi.finalcase.payment.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "payment_callbacks",
        indexes = {
                @Index(name = "idx_payment_callbacks_provider_event_key", columnList = "provider, event_key", unique = true),
                @Index(name = "idx_payment_callbacks_provider_token", columnList = "provider, provider_token"),
                @Index(name = "idx_payment_callbacks_processed", columnList = "processed")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PaymentCallback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentProviderCode provider;

    @Column(name = "event_key", nullable = false, length = 250)
    private String eventKey;

    @Column(name = "provider_token", length = 250)
    private String providerToken;

    @Column(nullable = false)
    private boolean processed;

    @Column(name = "processing_error", length = 1000)
    private String processingError;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> payloadSnapshot = new HashMap<>();

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    public PaymentCallback(
            PaymentProviderCode provider,
            String eventKey,
            String providerToken,
            Map<String, Object> payloadSnapshot
    ) {
        if (provider == null) {
            throw new BaseException(PaymentErrorCode.PAYMENT_CALLBACK_INVALID, "Provider is required");
        }

        validateRequired(eventKey, "Callback event key is required");

        this.provider = provider;
        this.eventKey = eventKey.trim();
        this.providerToken = normalize(providerToken);
        this.payloadSnapshot = payloadSnapshot == null
                ? new HashMap<>()
                : new HashMap<>(payloadSnapshot);
        this.processed = false;
    }

    public void markProcessed() {
        this.processed = true;
        this.processingError = null;
        this.processedAt = Instant.now();
    }

    public void markProcessingError(String processingError) {
        this.processed = false;
        this.processingError = normalize(processingError);
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(PaymentErrorCode.PAYMENT_CALLBACK_INVALID, message);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}