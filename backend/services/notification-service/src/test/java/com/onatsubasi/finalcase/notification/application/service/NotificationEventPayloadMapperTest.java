package com.onatsubasi.finalcase.notification.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.notification.application.dto.command.CreateNotificationCommand;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationReferenceType;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.infrastructure.messaging.NotificationEventTypes;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEventPayloadMapperTest {

    private final NotificationEventPayloadMapper mapper = new NotificationEventPayloadMapper(
            new ObjectMapper(),
            new NotificationChannelResolver()
    );

    @Test
    void mapsOrderPaidEventToCustomerNotificationCommand() {
        UUID userId = UUID.randomUUID();

        CreateNotificationCommand command = mapper.toCommand(
                NotificationEventTypes.ORDER_PAID,
                Map.of(
                        "userId", userId.toString(),
                        "orderId", "order-123",
                        "userEmail", "customer@example.com",
                        "orderNumber", "ORD-1"
                )
        );

        assertThat(command).isNotNull();
        assertThat(command.type()).isEqualTo(NotificationType.ORDER_PAID);
        assertThat(command.referenceType()).isEqualTo(NotificationReferenceType.ORDER);
        assertThat(command.referenceId()).isEqualTo("order-123");
        assertThat(command.recipientUserId()).isEqualTo(userId);
        assertThat(command.recipientEmail()).isEqualTo("customer@example.com");
        assertThat(command.channels()).isNotEmpty();
    }

    @Test
    void returnsNullForUnsupportedOrIncompleteEvent() {
        assertThat(mapper.toCommand("unknown.event", Map.of("userId", UUID.randomUUID().toString()))).isNull();
        assertThat(mapper.toCommand(NotificationEventTypes.ORDER_PAID, Map.of("orderId", "order-123"))).isNull();
    }
}
