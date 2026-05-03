package com.onatsubasi.finalcase.notification.application.dto.request;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to mark all matching notifications as read")
public record MarkAllNotificationsReadRequest(
        NotificationType type
) {
}