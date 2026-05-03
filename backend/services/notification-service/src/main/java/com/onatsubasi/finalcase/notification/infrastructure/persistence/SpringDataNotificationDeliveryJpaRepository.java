package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationDeliveryStatus;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationDelivery;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataNotificationDeliveryJpaRepository
        extends JpaRepository<NotificationDelivery, UUID> {

    Page<NotificationDelivery> findByNotification_Id(
            UUID notificationId,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from NotificationDelivery d where d.id = :id")
    Optional<NotificationDelivery> findByIdForUpdate(UUID id);

    @Query("""
           select d from NotificationDelivery d
            where d.status = :status
              and d.nextRetryAt is not null
              and d.nextRetryAt <= :now
            order by d.nextRetryAt asc
           """)
    List<NotificationDelivery> findDueRetryDeliveries(
            NotificationDeliveryStatus status,
            Instant now,
            Pageable pageable
    );
}