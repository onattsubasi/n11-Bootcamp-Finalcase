package com.onatsubasi.finalcase.notification.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Notification read response")
public record NotificationReadResponse(
        UUID notificationId,
        boolean read,
        Instant readAt
) {
}