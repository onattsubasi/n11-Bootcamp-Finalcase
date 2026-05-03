package com.onatsubasi.finalcase.notification.application.dto.response;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationReferenceType;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationStatus;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Notification summary response")
public record NotificationSummaryResponse(
        UUID id,
        UUID recipientUserId,
        NotificationType type,
        NotificationStatus status,
        NotificationReferenceType referenceType,
        String referenceId,
        String title,
        String message,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
}