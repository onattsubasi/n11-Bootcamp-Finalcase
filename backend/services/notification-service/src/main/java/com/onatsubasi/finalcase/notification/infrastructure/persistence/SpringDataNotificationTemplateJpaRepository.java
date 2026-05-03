package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataNotificationTemplateJpaRepository
        extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByTypeAndChannelAndLocale(
            NotificationType type,
            NotificationChannel channel,
            String locale
    );

    Optional<NotificationTemplate> findByTypeAndChannelAndLocaleAndActiveTrue(
            NotificationType type,
            NotificationChannel channel,
            String locale
    );
}