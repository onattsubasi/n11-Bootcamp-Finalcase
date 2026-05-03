package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.entity.NotificationProcessedEvent;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaNotificationProcessedEventRepository
        implements NotificationProcessedEventRepository {

    private final SpringDataNotificationProcessedEventJpaRepository springDataRepository;

    @Override
    public NotificationProcessedEvent save(NotificationProcessedEvent event) {
        return springDataRepository.save(event);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return springDataRepository.existsByEventId(eventId);
    }

    @Override
    public Optional<NotificationProcessedEvent> findByEventId(String eventId) {
        return springDataRepository.findByEventId(eventId);
    }
}