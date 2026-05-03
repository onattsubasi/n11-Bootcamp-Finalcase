package com.onatsubasi.finalcase.notification.application.dto.response;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationDeliveryStatus;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationProvider;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Notification delivery response")
public record NotificationDeliveryResponse(
        UUID id,
        NotificationChannel channel,
        NotificationProvider provider,
        String recipientAddress,
        NotificationDeliveryStatus status,
        int attemptCount,
        int maxAttempts,
        String providerMessageId,
        String lastError,
        Instant nextRetryAt,
        Instant sentAt,
        List<NotificationDeliveryAttemptResponse> attempts
) {
}