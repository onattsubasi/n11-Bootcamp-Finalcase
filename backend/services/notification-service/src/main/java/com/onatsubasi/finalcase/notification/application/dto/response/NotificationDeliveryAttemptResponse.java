package com.onatsubasi.finalcase.notification.application.dto.response;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationDeliveryAttemptStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Notification delivery attempt response")
public record NotificationDeliveryAttemptResponse(
        UUID id,
        int attemptNumber,
        NotificationDeliveryAttemptStatus status,
        String providerMessageId,
        String errorMessage,
        boolean retryable,
        Map<String, Object> requestSnapshot,
        Map<String, Object> responseSnapshot,
        Instant createdAt
) {
}