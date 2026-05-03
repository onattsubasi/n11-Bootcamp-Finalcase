package com.onatsubasi.finalcase.notification.domain.entity;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notification_preferences",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_preference_user_type_channel",
                        columnNames = {"user_id", "type", "channel"}
                )
        },
        indexes = {
                @Index(name = "idx_notification_preferences_user_id", columnList = "user_id"),
                @Index(name = "idx_notification_preferences_type", columnList = "type"),
                @Index(name = "idx_notification_preferences_enabled", columnList = "enabled")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationChannel channel;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean transactional;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public NotificationPreference(
            UUID userId,
            NotificationType type,
            NotificationChannel channel,
            boolean enabled,
            boolean transactional
    ) {
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.enabled = enabled;
        this.transactional = transactional;
    }

    public boolean canSend() {
        return transactional || enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        if (!transactional) {
            this.enabled = false;
        }
    }
}