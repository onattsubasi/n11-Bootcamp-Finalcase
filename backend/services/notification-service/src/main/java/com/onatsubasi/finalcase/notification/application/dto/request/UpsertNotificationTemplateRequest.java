package com.onatsubasi.finalcase.notification.application.dto.request;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request to create or update a notification template")
public record UpsertNotificationTemplateRequest(
        @NotNull(message = "Notification type is required")
        NotificationType type,

        @NotNull(message = "Notification channel is required")
        NotificationChannel channel,

        @NotBlank(message = "Locale is required")
        @Size(max = 10, message = "Locale cannot exceed 10 characters")
        String locale,

        @NotBlank(message = "Title template is required")
        @Size(max = 500, message = "Title template cannot exceed 500 characters")
        String titleTemplate,

        @NotBlank(message = "Message template is required")
        String messageTemplate,

        List<String> requiredVariables,

        Boolean active
) {
}