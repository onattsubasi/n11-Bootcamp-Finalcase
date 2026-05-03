package com.onatsubasi.finalcase.notification.application.dto.response;

import com.onatsubasi.finalcase.notification.domain.enums.ProcessedNotificationEventStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Processed notification event response")
public record ProcessedNotificationEventResponse(
        UUID id,
        String eventId,
        String eventType,
        ProcessedNotificationEventStatus status,
        String errorMessage,
        Instant processedAt
) {
}