package com.onatsubasi.finalcase.notification.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationDetailResponse;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationReadResponse;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationSummaryResponse;
import com.onatsubasi.finalcase.notification.application.dto.response.MarkAllNotificationsReadResponse;
import com.onatsubasi.finalcase.notification.application.dto.response.UnreadNotificationCountResponse;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationRepository;
import com.onatsubasi.finalcase.notification.infrastructure.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public Page<NotificationSummaryResponse> getMyNotifications(
            UUID userId,
            boolean unreadOnly,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return notificationRepository.findByRecipientUserId(
                userId,
                unreadOnly,
                pageable
        ).map(notificationMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public NotificationDetailResponse getMyNotification(
            UUID notificationId,
            UUID userId
    ) {
        Notification notification = notificationRepository
                .findByIdAndRecipientUserId(notificationId, userId)
                .orElseThrow(() -> new BaseException(
                        NotificationErrorCode.NOTIFICATION_NOT_FOUND
                ));

        return notificationMapper.toDetailResponse(notification);
    }

    @Transactional
    public NotificationReadResponse markRead(
            UUID notificationId,
            UUID userId
    ) {
        Notification notification = notificationRepository
                .findByIdForUpdate(notificationId)
                .orElseThrow(() -> new BaseException(
                        NotificationErrorCode.NOTIFICATION_NOT_FOUND
                ));

        if (!notification.getRecipientUserId().equals(userId)) {
            throw new BaseException(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        notification.markRead();

        Notification saved = notificationRepository.save(notification);

        return notificationMapper.toReadResponse(saved);
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount(UUID userId) {
        return new UnreadNotificationCountResponse(
                notificationRepository.countUnreadByRecipientUserId(userId)
        );
    }

    @Transactional
    public MarkAllNotificationsReadResponse markAllRead(UUID userId) {
        int updatedCount = notificationRepository.markAllReadByRecipientUserId(userId);

        log.info(
                "event=notification.mark_all_read userId={} updatedCount={}",
                userId,
                updatedCount
        );

        return new MarkAllNotificationsReadResponse(updatedCount);
    }

    @Transactional(readOnly = true)
    public Page<NotificationSummaryResponse> getAllNotifications(
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return notificationRepository.findAll(pageable)
                .map(notificationMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public NotificationDetailResponse getByIdForAdmin(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BaseException(
                        NotificationErrorCode.NOTIFICATION_NOT_FOUND
                ));

        return notificationMapper.toDetailResponse(notification);
    }
}