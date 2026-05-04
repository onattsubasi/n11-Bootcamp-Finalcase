package com.onatsubasi.finalcase.notification.domain.repository;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationTemplate;

import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository {

    NotificationTemplate save(NotificationTemplate template);

    Optional<NotificationTemplate> findById(UUID id);

    Optional<NotificationTemplate> findByTypeAndChannelAndLocale(
            NotificationType type,
            NotificationChannel channel,
            String locale
    );

    Optional<NotificationTemplate> findActiveTemplate(
            NotificationType type,
            NotificationChannel channel,
            String locale
    );
}