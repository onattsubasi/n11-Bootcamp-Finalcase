package com.onatsubasi.finalcase.notification.application.dto.response;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationReferenceType;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationStatus;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Notification detail response")
public record NotificationDetailResponse(
        UUID id,
        UUID recipientUserId,
        String recipientEmail,
        String recipientPhone,
        NotificationType type,
        NotificationStatus status,
        NotificationReferenceType referenceType,
        String referenceId,
        String locale,
        String title,
        String message,
        Map<String, Object> payloadSnapshot,
        boolean read,
        Instant readAt,
        List<NotificationDeliveryResponse> deliveries,
        Instant createdAt,
        Instant updatedAt
) {
}