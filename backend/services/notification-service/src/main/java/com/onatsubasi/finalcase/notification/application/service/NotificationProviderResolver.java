package com.onatsubasi.finalcase.notification.application.service;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationProvider;
import org.springframework.stereotype.Component;

@Component
public class NotificationProviderResolver {

    public NotificationProvider resolve(NotificationChannel channel) {
        return switch (channel) {
            case IN_APP -> NotificationProvider.IN_APP;
            case EMAIL -> NotificationProvider.MOCK_EMAIL;
            case SMS -> NotificationProvider.TWILIO;
            case PUSH -> NotificationProvider.FCM;
            case WEBHOOK -> NotificationProvider.WEBHOOK;
        };
    }
}