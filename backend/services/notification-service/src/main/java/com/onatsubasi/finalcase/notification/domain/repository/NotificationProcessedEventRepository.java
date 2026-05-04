package com.onatsubasi.finalcase.notification.domain.repository;

import com.onatsubasi.finalcase.notification.domain.entity.NotificationProcessedEvent;

import java.util.Optional;

public interface NotificationProcessedEventRepository {

    NotificationProcessedEvent save(NotificationProcessedEvent event);

    boolean existsByEventId(String eventId);

    Optional<NotificationProcessedEvent> findByEventId(String eventId);
}