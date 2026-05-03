package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationPreference;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaNotificationPreferenceRepository
        implements NotificationPreferenceRepository {

    private final SpringDataNotificationPreferenceJpaRepository springDataRepository;

    @Override
    public NotificationPreference save(NotificationPreference preference) {
        return springDataRepository.save(preference);
    }

    @Override
    public Optional<NotificationPreference> findByUserIdAndTypeAndChannel(
            UUID userId,
            NotificationType type,
            NotificationChannel channel
    ) {
        return springDataRepository.findByUserIdAndTypeAndChannel(
                userId,
                type,
                channel
        );
    }
}