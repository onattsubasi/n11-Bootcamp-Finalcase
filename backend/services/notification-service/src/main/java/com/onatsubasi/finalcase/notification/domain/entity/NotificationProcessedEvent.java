package com.onatsubasi.finalcase.notification.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.domain.enums.ProcessedNotificationEventStatus;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notification_processed_events",
        indexes = {
                @Index(name = "idx_notification_processed_events_event_id", columnList = "event_id", unique = true),
                @Index(name = "idx_notification_processed_events_event_type", columnList = "event_type"),
                @Index(name = "idx_notification_processed_events_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class NotificationProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true, length = 120)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProcessedNotificationEventStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    public NotificationProcessedEvent(
            String eventId,
            String eventType,
            ProcessedNotificationEventStatus status,
            String errorMessage
    ) {
        validateRequired(eventId, "Event id is required");
        validateRequired(eventType, "Event type is required");

        this.eventId = eventId.trim();
        this.eventType = eventType.trim();
        this.status = status == null ? ProcessedNotificationEventStatus.PROCESSED : status;
        this.errorMessage = normalize(errorMessage);
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(NotificationErrorCode.NOTIFICATION_EVENT_INVALID, message);
        }
    }

    public void updateStatus(
            ProcessedNotificationEventStatus status,
            String errorMessage
    ) {
        this.status = status == null ? ProcessedNotificationEventStatus.PROCESSED : status;
        this.errorMessage = normalize(errorMessage);
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}