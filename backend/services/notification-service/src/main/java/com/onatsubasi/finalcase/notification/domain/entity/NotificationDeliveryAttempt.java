package com.onatsubasi.finalcase.notification.domain.entity;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationDeliveryAttemptStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "notification_delivery_attempts",
        indexes = {
                @Index(name = "idx_notification_delivery_attempts_delivery_id", columnList = "delivery_id"),
                @Index(name = "idx_notification_delivery_attempts_status", columnList = "status"),
                @Index(name = "idx_notification_delivery_attempts_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class NotificationDeliveryAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private NotificationDelivery delivery;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationDeliveryAttemptStatus status;

    @Column(name = "provider_message_id", length = 200)
    private String providerMessageId;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retryable", nullable = false)
    private boolean retryable;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> requestSnapshot = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> responseSnapshot = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public NotificationDeliveryAttempt(
            NotificationDelivery delivery,
            int attemptNumber,
            NotificationDeliveryAttemptStatus status,
            String providerMessageId,
            String errorMessage,
            boolean retryable,
            Map<String, Object> requestSnapshot,
            Map<String, Object> responseSnapshot
    ) {
        this.delivery = delivery;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.providerMessageId = normalize(providerMessageId);
        this.errorMessage = normalize(errorMessage);
        this.retryable = retryable;
        this.requestSnapshot = requestSnapshot == null ? new HashMap<>() : new HashMap<>(requestSnapshot);
        this.responseSnapshot = responseSnapshot == null ? new HashMap<>() : new HashMap<>(responseSnapshot);
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}