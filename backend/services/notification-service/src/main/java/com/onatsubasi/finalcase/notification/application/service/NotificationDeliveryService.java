package com.onatsubasi.finalcase.notification.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.application.dto.provider.NotificationProviderSendCommand;
import com.onatsubasi.finalcase.notification.application.dto.provider.NotificationProviderSendResult;
import com.onatsubasi.finalcase.notification.application.port.NotificationChannelProviderPort;
import com.onatsubasi.finalcase.notification.application.port.NotificationEventPublisher;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationDeliveryStatus;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationDelivery;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationDeliveryRepository;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationRepository;
import com.onatsubasi.finalcase.notification.infrastructure.config.NotificationServiceProperties;
import com.onatsubasi.finalcase.notification.infrastructure.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationProviderFactory providerFactory;
    private final NotificationMapper notificationMapper;
    private final NotificationEventPublisher eventPublisher;
    private final NotificationServiceProperties properties;

    @Transactional
    public void sendDelivery(UUID deliveryId) {
        NotificationDelivery delivery = deliveryRepository.findByIdForUpdate(deliveryId)
                .orElseThrow(() -> new BaseException(
                        NotificationErrorCode.NOTIFICATION_DELIVERY_NOT_FOUND
                ));

        if (delivery.getStatus() == NotificationDeliveryStatus.SENT
                || delivery.getStatus() == NotificationDeliveryStatus.SKIPPED) {
            return;
        }

        Notification notification = delivery.getNotification();

        NotificationChannelProviderPort provider =
                providerFactory.getProvider(delivery.getProvider());

        NotificationProviderSendCommand command =
                notificationMapper.toProviderSendCommand(notification, delivery);

        NotificationProviderSendResult result = provider.send(command);

        Map<String, Object> requestSnapshot = notificationMapper.toMap(command);
        Map<String, Object> responseSnapshot = notificationMapper.toMap(result);

        if (result.success()) {
            delivery.markSent(
                    result.providerMessageId(),
                    requestSnapshot,
                    responseSnapshot
            );

            deliveryRepository.save(delivery);

            notification.refreshDeliveryStatus();
            Notification savedNotification = notificationRepository.save(notification);

            eventPublisher.publishNotificationSent(savedNotification);

            log.info(
                    "event=notification.delivery_sent notificationId={} deliveryId={} provider={} channel={}",
                    notification.getId(),
                    delivery.getId(),
                    delivery.getProvider(),
                    delivery.getChannel()
            );

            return;
        }

        Instant nextRetryAt = calculateNextRetryAt(delivery.getAttemptCount() + 1);

        delivery.markFailed(
                result.errorMessage(),
                result.retryable(),
                nextRetryAt,
                requestSnapshot,
                responseSnapshot
        );

        deliveryRepository.save(delivery);

        notification.refreshDeliveryStatus();
        Notification savedNotification = notificationRepository.save(notification);

        if (delivery.getStatus() == NotificationDeliveryStatus.RETRY_SCHEDULED) {
            eventPublisher.publishNotificationDeliveryRetryScheduled(
                    savedNotification,
                    delivery
            );
        } else {
            eventPublisher.publishNotificationFailed(savedNotification);
        }

        log.warn(
                "event=notification.delivery_failed notificationId={} deliveryId={} provider={} retryable={} status={} error={}",
                notification.getId(),
                delivery.getId(),
                delivery.getProvider(),
                result.retryable(),
                delivery.getStatus(),
                result.errorMessage()
        );
    }

    @Transactional
    public void retryDueDeliveries() {
        var deliveries = deliveryRepository.findDueRetryDeliveries(
                NotificationDeliveryStatus.RETRY_SCHEDULED,
                Instant.now(),
                properties.getRetryBatchSize()
        );

        log.info(
                "event=notification.retry_due_deliveries_started count={}",
                deliveries.size()
        );

        deliveries.forEach(delivery -> sendDelivery(delivery.getId()));
    }

    @Transactional
    public void retryDelivery(UUID deliveryId) {
        NotificationDelivery delivery = deliveryRepository.findByIdForUpdate(deliveryId)
                .orElseThrow(() -> new BaseException(
                        NotificationErrorCode.NOTIFICATION_DELIVERY_NOT_FOUND
                ));

        delivery.resetForRetry();
        deliveryRepository.save(delivery);

        sendDelivery(deliveryId);
    }

    private Instant calculateNextRetryAt(int nextAttemptNumber) {
        int delaySeconds = switch (nextAttemptNumber) {
            case 1 -> properties.getFirstRetryDelaySeconds();
            case 2 -> properties.getSecondRetryDelaySeconds();
            default -> properties.getFallbackRetryDelaySeconds();
        };

        return Instant.now().plusSeconds(delaySeconds);
    }
}