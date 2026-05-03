package com.onatsubasi.finalcase.notification.infrastructure.provider.email;

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
public class MockEmailNotificationProviderAdapter
        implements NotificationChannelProviderPort {

    @Override
    public NotificationProvider provider() {
        return NotificationProvider.MOCK_EMAIL;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public NotificationProviderSendResult send(
            NotificationProviderSendCommand command
    ) {
        log.info(
                "event=notification.mock_email_send notificationId={} deliveryId={} destination={} title={}",
                command.notificationId(),
                command.deliveryId(),
                command.destination(),
                command.title()
        );

        if (command.destination() == null || command.destination().isBlank()) {
            return new NotificationProviderSendResult(
                    false,
                    false,
                    null,
                    "INVALID_EMAIL",
                    "Email destination is missing",
                    Map.of()
            );
        }

        return new NotificationProviderSendResult(
                true,
                false,
                "MOCKEMAIL-" + UUID.randomUUID(),
                null,
                null,
                Map.of(
                        "mock", true,
                        "destination", command.destination()
                )
        );
    }
}