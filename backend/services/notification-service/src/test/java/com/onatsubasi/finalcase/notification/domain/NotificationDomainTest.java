package com.onatsubasi.finalcase.notification.domain;

import com.onatsubasi.finalcase.notification.domain.enums.*;
import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationDelivery;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDomainTest {

    @Test
    void markReadIsIdempotent() {
        Notification notification = notification();

        notification.markRead();
        Instant firstReadAt = notification.getReadAt();
        notification.markRead();

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isEqualTo(firstReadAt);
    }

    @Test
    void refreshDeliveryStatusMarksNotificationAsSentWhenAllDeliveriesAreSent() {
        Notification notification = notification();
        NotificationDelivery inApp = new NotificationDelivery(NotificationChannel.IN_APP, NotificationProvider.IN_APP, notification.getRecipientUserId().toString(), 3);
        NotificationDelivery email = new NotificationDelivery(NotificationChannel.EMAIL, NotificationProvider.MOCK_EMAIL, "customer@example.com", 3);
        notification.addDelivery(inApp);
        notification.addDelivery(email);

        inApp.markSent("inapp-1", Map.of(), Map.of());
        email.markSent("email-1", Map.of(), Map.of());
        notification.refreshDeliveryStatus();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void retryableFailureSchedulesRetryUntilMaxAttemptsThenGivesUp() {
        NotificationDelivery delivery = new NotificationDelivery(NotificationChannel.EMAIL, NotificationProvider.MOCK_EMAIL, "customer@example.com", 2);

        delivery.markFailed("temporary", true, Instant.now().plusSeconds(30), Map.of(), Map.of());
        assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.RETRY_SCHEDULED);
        assertThat(delivery.getAttemptCount()).isEqualTo(1);

        delivery.resetForRetry();
        delivery.markFailed("still failing", true, Instant.now().plusSeconds(30), Map.of(), Map.of());

        assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.GAVE_UP);
        assertThat(delivery.getAttemptCount()).isEqualTo(2);
    }

    private Notification notification() {
        return new Notification(
                UUID.randomUUID(),
                "customer@example.com",
                null,
                NotificationType.ORDER_PAID,
                NotificationReferenceType.ORDER,
                "order-1",
                "tr",
                "Order paid",
                "Your order was paid",
                Map.of("orderNumber", "ORD-1")
        );
    }
}
