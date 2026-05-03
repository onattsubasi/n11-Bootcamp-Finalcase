package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataNotificationJpaRepository
        extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByIdAndRecipientUserId(
            UUID id,
            UUID recipientUserId
    );

    @Query("""
           select n from Notification n
            where n.recipientUserId = :recipientUserId
              and (:unreadOnly = false or n.readAt is null)
           """)
    Page<Notification> findByRecipientUserId(
            @Param("recipientUserId") UUID recipientUserId,
            @Param("unreadOnly") boolean unreadOnly,
            Pageable pageable
    );

    long countByRecipientUserIdAndReadAtIsNull(UUID recipientUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Notification n where n.id = :id")
    Optional<Notification> findByIdForUpdate(UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Notification n
              set n.readAt = CURRENT_TIMESTAMP
            where n.recipientUserId = :recipientUserId
              and n.readAt is null
           """)
    int markAllReadByRecipientUserId(@Param("recipientUserId") UUID recipientUserId);
}