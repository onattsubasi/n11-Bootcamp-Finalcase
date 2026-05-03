package com.onatsubasi.finalcase.notification.application.dto.request;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationStatus;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Notification search filters")
public record NotificationSearchRequest(
        NotificationType type,
        NotificationStatus status,
        Boolean unreadOnly
) {
}