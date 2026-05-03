package com.onatsubasi.finalcase.notification.application.dto.response;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Notification template response")
public record NotificationTemplateResponse(
        UUID id,
        NotificationType type,
        NotificationChannel channel,
        String locale,
        String titleTemplate,
        String messageTemplate,
        List<String> requiredVariables,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}