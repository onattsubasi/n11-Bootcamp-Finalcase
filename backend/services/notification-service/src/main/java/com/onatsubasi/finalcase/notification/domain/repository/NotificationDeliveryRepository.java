package com.onatsubasi.finalcase.notification.domain.repository;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationDeliveryStatus;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationDeliveryRepository {

    NotificationDelivery save(NotificationDelivery delivery);

    Optional<NotificationDelivery> findById(UUID id);

    Optional<NotificationDelivery> findByIdForUpdate(UUID id);

    List<NotificationDelivery> findDueRetryDeliveries(
            NotificationDeliveryStatus status,
            Instant now,
            int limit
    );

    Page<NotificationDelivery> findByNotificationId(UUID notificationId, Pageable pageable);
}