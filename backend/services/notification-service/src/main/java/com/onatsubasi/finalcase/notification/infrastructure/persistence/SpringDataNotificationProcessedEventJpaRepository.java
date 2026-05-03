package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.entity.NotificationProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataNotificationProcessedEventJpaRepository
        extends JpaRepository<NotificationProcessedEvent, UUID> {

    boolean existsByEventId(String eventId);

    Optional<NotificationProcessedEvent> findByEventId(String eventId);
}