package com.onatsubasi.finalcase.notification.application.dto.request;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationReferenceType;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Internal request to create a direct notification")
public record CreateDirectNotificationRequest(
        @NotNull(message = "Notification type is required")
        NotificationType type,

        @NotNull(message = "Recipient user id is required")
        UUID recipientUserId,

        @Size(max = 320, message = "Recipient email cannot exceed 320 characters")
        String recipientEmail,

        @Size(max = 50, message = "Recipient phone cannot exceed 50 characters")
        String recipientPhone,

        @NotEmpty(message = "At least one notification channel is required")
        List<NotificationChannel> channels,

        @Size(max = 10, message = "Locale cannot exceed 10 characters")
        String locale,

        Map<String, Object> templateVariables,

        @NotNull(message = "Reference type is required")
        NotificationReferenceType referenceType,

        @Size(max = 100, message = "Reference id cannot exceed 100 characters")
        String referenceId
) {
}