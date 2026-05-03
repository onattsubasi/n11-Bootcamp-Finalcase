package com.onatsubasi.finalcase.notification.application.dto.command;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationReferenceType;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateNotificationCommand(
        NotificationType type,
        UUID recipientUserId,
        String recipientEmail,
        String recipientPhone,
        List<NotificationChannel> channels,
        String locale,
        NotificationReferenceType referenceType,
        String referenceId,
        Map<String, Object> templateVariables,
        Map<String, Object> payloadSnapshot
) {
}