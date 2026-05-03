package com.onatsubasi.finalcase.notification.application.dto.provider;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationReferenceType;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationStatus;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record NotificationChangedEvent(
        UUID notificationId,
        UUID recipientUserId,
        NotificationType type,
        NotificationStatus status,
        NotificationReferenceType referenceType,
        String referenceId,
        boolean read,
        Instant readAt,
        Instant createdAt
) {

    public static NotificationChangedEvent from(Notification notification) {
        return NotificationChangedEvent.builder()
                .notificationId(notification.getId())
                .recipientUserId(notification.getRecipientUserId())
                .type(notification.getType())
                .status(notification.getStatus())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}