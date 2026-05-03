package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationTemplate;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaNotificationTemplateRepository
        implements NotificationTemplateRepository {

    private final SpringDataNotificationTemplateJpaRepository springDataRepository;

    @Override
    public NotificationTemplate save(NotificationTemplate template) {
        return springDataRepository.save(template);
    }

    @Override
    public Optional<NotificationTemplate> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<NotificationTemplate> findByTypeAndChannelAndLocale(
            NotificationType type,
            NotificationChannel channel,
            String locale
    ) {
        return springDataRepository.findByTypeAndChannelAndLocale(
                type,
                channel,
                locale
        );
    }

    @Override
    public Optional<NotificationTemplate> findActiveTemplate(
            NotificationType type,
            NotificationChannel channel,
            String locale
    ) {
        return springDataRepository.findByTypeAndChannelAndLocaleAndActiveTrue(
                type,
                channel,
                locale
        );
    }
}