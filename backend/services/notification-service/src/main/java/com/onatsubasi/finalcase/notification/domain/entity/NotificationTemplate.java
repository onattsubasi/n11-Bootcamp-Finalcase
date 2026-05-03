package com.onatsubasi.finalcase.notification.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(
        name = "notification_templates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_template_type_channel_locale",
                        columnNames = {"type", "channel", "locale"}
                )
        },
        indexes = {
                @Index(name = "idx_notification_templates_type", columnList = "type"),
                @Index(name = "idx_notification_templates_channel", columnList = "channel"),
                @Index(name = "idx_notification_templates_locale", columnList = "locale"),
                @Index(name = "idx_notification_templates_active", columnList = "active")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationChannel channel;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(name = "title_template", nullable = false, length = 500)
    private String titleTemplate;

    @Column(name = "message_template", nullable = false, columnDefinition = "text")
    private String messageTemplate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_variables", columnDefinition = "jsonb")
    private List<String> requiredVariables = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public NotificationTemplate(
            NotificationType type,
            NotificationChannel channel,
            String locale,
            String titleTemplate,
            String messageTemplate,
            List<String> requiredVariables,
            boolean active
    ) {
        if (type == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, "Notification type is required");
        }

        if (channel == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, "Notification channel is required");
        }

        validateRequired(locale, "Locale is required");
        validateRequired(titleTemplate, "Title template is required");
        validateRequired(messageTemplate, "Message template is required");

        this.type = type;
        this.channel = channel;
        this.locale = locale.trim().toLowerCase(Locale.ROOT);
        this.titleTemplate = titleTemplate.trim();
        this.messageTemplate = messageTemplate.trim();
        this.requiredVariables = requiredVariables == null ? new ArrayList<>() : new ArrayList<>(requiredVariables);
        this.active = active;
    }

    public void updateContent(
            String titleTemplate,
            String messageTemplate,
            List<String> requiredVariables
    ) {
        validateRequired(titleTemplate, "Title template is required");
        validateRequired(messageTemplate, "Message template is required");

        this.titleTemplate = titleTemplate.trim();
        this.messageTemplate = messageTemplate.trim();
        this.requiredVariables = requiredVariables == null ? new ArrayList<>() : new ArrayList<>(requiredVariables);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_DATA, message);
        }
    }
}