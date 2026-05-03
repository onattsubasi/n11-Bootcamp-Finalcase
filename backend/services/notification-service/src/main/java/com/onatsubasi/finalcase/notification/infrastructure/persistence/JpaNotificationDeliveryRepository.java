package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationDeliveryStatus;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationDelivery;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaNotificationDeliveryRepository
        implements NotificationDeliveryRepository {

    private final SpringDataNotificationDeliveryJpaRepository springDataRepository;

    @Override
    public NotificationDelivery save(NotificationDelivery delivery) {
        return springDataRepository.save(delivery);
    }

    @Override
    public Optional<NotificationDelivery> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<NotificationDelivery> findByIdForUpdate(UUID id) {
        return springDataRepository.findByIdForUpdate(id);
    }

    @Override
    public List<NotificationDelivery> findDueRetryDeliveries(
            NotificationDeliveryStatus status,
            Instant now,
            int limit
    ) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));

        return springDataRepository.findDueRetryDeliveries(
                status,
                now,
                pageable
        );
    }

    @Override
    public Page<NotificationDelivery> findByNotificationId(
            UUID notificationId,
            Pageable pageable
    ) {
        return springDataRepository.findByNotification_Id(
                notificationId,
                pageable
        );
    }
}