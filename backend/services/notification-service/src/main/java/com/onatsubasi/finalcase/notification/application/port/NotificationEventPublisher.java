package com.onatsubasi.finalcase.notification.application.port;

import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationDelivery;

public interface NotificationEventPublisher {

    void publishNotificationCreated(Notification notification);

    void publishNotificationSent(Notification notification);

    void publishNotificationFailed(Notification notification);

    void publishNotificationDeliveryRetryScheduled(Notification notification, NotificationDelivery delivery);
}