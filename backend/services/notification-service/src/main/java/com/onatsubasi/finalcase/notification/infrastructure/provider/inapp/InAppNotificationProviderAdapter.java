package com.onatsubasi.finalcase.notification.infrastructure.provider.inapp;

import com.onatsubasi.finalcase.notification.application.dto.provider.NotificationProviderSendCommand;
import com.onatsubasi.finalcase.notification.application.dto.provider.NotificationProviderSendResult;
import com.onatsubasi.finalcase.notification.application.port.NotificationChannelProviderPort;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class InAppNotificationProviderAdapter
        implements NotificationChannelProviderPort {

    @Override
    public NotificationProvider provider() {
        return NotificationProvider.IN_APP;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public NotificationProviderSendResult send(
            NotificationProviderSendCommand command
    ) {
        log.info(
                "event=notification.in_app_send notificationId={} deliveryId={} recipient={}",
                command.notificationId(),
                command.deliveryId(),
                command.destination()
        );

        return new NotificationProviderSendResult(
                true,
                false,
                "INAPP-" + UUID.randomUUID(),
                null,
                null,
                Map.of("stored", true)
        );
    }
}