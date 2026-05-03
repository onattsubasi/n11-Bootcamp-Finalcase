package com.onatsubasi.finalcase.notification.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationReferenceType;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationStatus;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient_user_id", columnList = "recipient_user_id"),
                @Index(name = "idx_notifications_type", columnList = "type"),
                @Index(name = "idx_notifications_status", columnList = "status"),
                @Index(name = "idx_notifications_reference", columnList = "reference_type, reference_id"),
                @Index(name = "idx_notifications_read_at", columnList = "read_at"),
                @Index(name = "idx_notifications_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "recipient_email", length = 320)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 50)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 40)
    private NotificationReferenceType referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> payloadSnapshot = new HashMap<>();

    @Column(name = "read_at")
    private Instant readAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationDelivery> deliveries = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Notification(
            UUID recipientUserId,
            String recipientEmail,
            String recipientPhone,
            NotificationType type,
            NotificationReferenceType referenceType,
            String referenceId,
            String locale,
            String title,
            String message,
            Map<String, Object> payloadSnapshot
    ) {
        if (recipientUserId == null) {
            throw new BaseException(NotificationErrorCode.INVALID_RECIPIENT, "Recipient user id is required");
        }

        if (type == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, "Notification type is required");
        }

        if (referenceType == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, "Reference type is required");
        }

        validateRequired(locale, "Locale is required");
        validateRequired(title, "Notification title is required");
        validateRequired(message, "Notification message is required");

        this.recipientUserId = recipientUserId;
        this.recipientEmail = normalize(recipientEmail);
        this.recipientPhone = normalize(recipientPhone);
        this.type = type;
        this.status = NotificationStatus.CREATED;
        this.referenceType = referenceType;
        this.referenceId = normalize(referenceId);
        this.locale = locale.trim();
        this.title = title.trim();
        this.message = message.trim();
        this.payloadSnapshot = payloadSnapshot == null ? new HashMap<>() : new HashMap<>(payloadSnapshot);
    }

    public void addDelivery(NotificationDelivery delivery) {
        if (delivery == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, "Notification delivery is required");
        }

        deliveries.add(delivery);
        delivery.assignTo(this);
    }

    public void markRead() {
        if (readAt == null) {
            this.readAt = Instant.now();
        }
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markTemplateRenderFailed(String reason) {
        this.status = NotificationStatus.TEMPLATE_RENDER_FAILED;
        this.message = reason == null || reason.isBlank()
                ? this.message
                : reason.trim();
    }

    public void refreshDeliveryStatus() {
        if (deliveries.isEmpty()) {
            this.status = NotificationStatus.CREATED;
            return;
        }

        long sentCount = deliveries.stream()
                .filter(delivery -> delivery.getStatus().name().equals("SENT"))
                .count();

        long failedCount = deliveries.stream()
                .filter(delivery -> delivery.getStatus().name().equals("FAILED")
                        || delivery.getStatus().name().equals("GAVE_UP"))
                .count();

        if (sentCount == deliveries.size()) {
            this.status = NotificationStatus.SENT;
            return;
        }

        if (failedCount == deliveries.size()) {
            this.status = NotificationStatus.FAILED;
            return;
        }

        if (sentCount > 0) {
            this.status = NotificationStatus.PARTIALLY_SENT;
        }
    }

    public List<NotificationDelivery> getDeliveries() {
        return List.copyOf(deliveries);
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, message);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}