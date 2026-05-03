package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaNotificationRepository implements NotificationRepository {

    private final SpringDataNotificationJpaRepository springDataRepository;

    @Override
    public Notification save(Notification notification) {
        return springDataRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Notification> findByIdForUpdate(UUID id) {
        return springDataRepository.findByIdForUpdate(id);
    }

    @Override
    public Optional<Notification> findByIdAndRecipientUserId(
            UUID id,
            UUID recipientUserId
    ) {
        return springDataRepository.findByIdAndRecipientUserId(
                id,
                recipientUserId
        );
    }

    @Override
    public Page<Notification> findByRecipientUserId(
            UUID recipientUserId,
            boolean unreadOnly,
            Pageable pageable
    ) {
        return springDataRepository.findByRecipientUserId(
                recipientUserId,
                unreadOnly,
                pageable
        );
    }

    @Override
    public Page<Notification> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }

    @Override
    public long countUnreadByRecipientUserId(UUID recipientUserId) {
        return springDataRepository.countByRecipientUserIdAndReadAtIsNull(
                recipientUserId
        );
    }

    @Override
    public int markAllReadByRecipientUserId(UUID recipientUserId) {
        return springDataRepository.markAllReadByRecipientUserId(recipientUserId);
    }
}