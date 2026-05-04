package com.onatsubasi.finalcase.notification.domain.repository;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationPreference;

import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepository {

    NotificationPreference save(NotificationPreference preference);

    Optional<NotificationPreference> findByUserIdAndTypeAndChannel(
            UUID userId,
            NotificationType type,
            NotificationChannel channel
    );
}