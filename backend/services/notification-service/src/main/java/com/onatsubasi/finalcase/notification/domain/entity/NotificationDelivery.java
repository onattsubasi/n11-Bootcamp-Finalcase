package com.onatsubasi.finalcase.notification.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationDeliveryAttemptStatus;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationDeliveryStatus;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationProvider;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.*;

@Entity
@Table(
        name = "notification_deliveries",
        indexes = {
                @Index(name = "idx_notification_deliveries_notification_id", columnList = "notification_id"),
                @Index(name = "idx_notification_deliveries_status", columnList = "status"),
                @Index(name = "idx_notification_deliveries_channel", columnList = "channel"),
                @Index(name = "idx_notification_deliveries_next_retry_at", columnList = "next_retry_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class NotificationDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private NotificationProvider provider;

    @Column(name = "recipient_address", length = 320)
    private String recipientAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationDeliveryStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "provider_message_id", length = 200)
    private String providerMessageId;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationDeliveryAttempt> attempts = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public NotificationDelivery(
            NotificationChannel channel,
            NotificationProvider provider,
            String recipientAddress,
            int maxAttempts
    ) {
        if (channel == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, "Notification channel is required");
        }

        if (provider == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, "Notification provider is required");
        }

        this.channel = channel;
        this.provider = provider;
        this.recipientAddress = normalize(recipientAddress);
        this.status = NotificationDeliveryStatus.PENDING;
        this.attemptCount = 0;
        this.maxAttempts = Math.max(maxAttempts, 1);
    }

    void assignTo(Notification notification) {
        if (notification == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, "Delivery must belong to notification");
        }

        this.notification = notification;
    }

    public void markSent(
            String providerMessageId,
            Map<String, Object> requestSnapshot,
            Map<String, Object> responseSnapshot
    ) {
        this.attemptCount++;
        this.status = NotificationDeliveryStatus.SENT;
        this.providerMessageId = normalize(providerMessageId);
        this.lastError = null;
        this.nextRetryAt = null;
        this.sentAt = Instant.now();

        attempts.add(new NotificationDeliveryAttempt(
                this,
                attemptCount,
                NotificationDeliveryAttemptStatus.SUCCESS,
                providerMessageId,
                null,
                false,
                requestSnapshot,
                responseSnapshot
        ));
    }

    public void markFailed(
            String errorMessage,
            boolean retryable,
            Instant nextRetryAt,
            Map<String, Object> requestSnapshot,
            Map<String, Object> responseSnapshot
    ) {
        this.attemptCount++;
        this.lastError = normalize(errorMessage);

        attempts.add(new NotificationDeliveryAttempt(
                this,
                attemptCount,
                NotificationDeliveryAttemptStatus.FAILED,
                null,
                errorMessage,
                retryable,
                requestSnapshot,
                responseSnapshot
        ));

        if (retryable && attemptCount < maxAttempts) {
            this.status = NotificationDeliveryStatus.RETRY_SCHEDULED;
            this.nextRetryAt = nextRetryAt;
            return;
        }

        if (retryable) {
            this.status = NotificationDeliveryStatus.GAVE_UP;
        } else {
            this.status = NotificationDeliveryStatus.FAILED;
        }

        this.nextRetryAt = null;
    }

    public void resetForRetry() {
        if (status != NotificationDeliveryStatus.FAILED
                && status != NotificationDeliveryStatus.RETRY_SCHEDULED
                && status != NotificationDeliveryStatus.GAVE_UP) {
            throw new BaseException(NotificationErrorCode.NOTIFICATION_DELIVERY_NOT_RETRYABLE);
        }

        this.status = NotificationDeliveryStatus.PENDING;
        this.nextRetryAt = null;
    }

    public boolean isTerminal() {
        return status == NotificationDeliveryStatus.SENT
                || status == NotificationDeliveryStatus.FAILED
                || status == NotificationDeliveryStatus.GAVE_UP
                || status == NotificationDeliveryStatus.SKIPPED;
    }

    public List<NotificationDeliveryAttempt> getAttempts() {
        return List.copyOf(attempts);
    }

    public Map<String, Object> emptySnapshot() {
        return new HashMap<>();
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}