package com.onatsubasi.finalcase.notification.application.dto.provider;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationProvider;

import java.util.Map;
import java.util.UUID;

public record NotificationProviderSendCommand(
        UUID notificationId,
        UUID deliveryId,
        NotificationChannel channel,
        NotificationProvider provider,
        String destination,
        String title,
        String message,
        Map<String, Object> metadata
) {
}