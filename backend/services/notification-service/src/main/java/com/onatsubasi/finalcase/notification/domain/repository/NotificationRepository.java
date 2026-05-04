package com.onatsubasi.finalcase.notification.domain.repository;

import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    Optional<Notification> findByIdForUpdate(UUID id);

    Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

    Page<Notification> findByRecipientUserId(UUID recipientUserId, boolean unreadOnly, Pageable pageable);

    Page<Notification> findAll(Pageable pageable);

    long countUnreadByRecipientUserId(UUID recipientUserId);

    int markAllReadByRecipientUserId(UUID recipientUserId);
}
