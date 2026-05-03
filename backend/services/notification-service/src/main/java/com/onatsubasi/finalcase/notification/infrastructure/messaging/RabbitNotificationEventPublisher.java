package com.onatsubasi.finalcase.notification.infrastructure.messaging;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.notification.application.dto.provider.NotificationChangedEvent;
import com.onatsubasi.finalcase.notification.application.port.NotificationEventPublisher;
import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationDelivery;
import com.onatsubasi.finalcase.notification.infrastructure.mapper.NotificationMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitNotificationEventPublisher
        implements NotificationEventPublisher {

    private static final String SOURCE = "notification-service";

    private final RabbitTemplate rabbitTemplate;
    private final NotificationMapper notificationMapper;

    @Override
    public void publishNotificationCreated(Notification notification) {
        publish(NotificationEventTypes.NOTIFICATION_CREATED, notification);
    }

    @Override
    public void publishNotificationSent(Notification notification) {
        publish(NotificationEventTypes.NOTIFICATION_SENT, notification);
    }

    @Override
    public void publishNotificationFailed(Notification notification) {
        publish(NotificationEventTypes.NOTIFICATION_FAILED, notification);
    }

    @Override
    public void publishNotificationDeliveryRetryScheduled(
            Notification notification,
            NotificationDelivery delivery
    ) {
        publish(
                NotificationEventTypes.NOTIFICATION_DELIVERY_RETRY_SCHEDULED,
                notification
        );
    }

    private void publish(
            String eventType,
            Notification notification
    ) {
        String correlationId = currentCorrelationId();

        NotificationChangedEvent payload =
                notificationMapper.toChangedEvent(notification);

        EventEnvelope<NotificationChangedEvent> envelope = EventEnvelope.of(
                eventType,
                SOURCE,
                correlationId,
                payload
        );

        Runnable sendTask = () -> {
            rabbitTemplate.convertAndSend(
                    EventBrokerConstants.MAIN_EXCHANGE,
                    eventType,
                    envelope,
                    message -> {
                        message.getMessageProperties()
                                .setHeader(
                                        EventBrokerConstants.EVENT_ID_HEADER,
                                        envelope.eventId()
                                );

                        message.getMessageProperties()
                                .setHeader(
                                        EventBrokerConstants.EVENT_TYPE_HEADER,
                                        envelope.eventType()
                                );

                        if (correlationId != null) {
                            message.getMessageProperties()
                                    .setHeader(
                                            EventBrokerConstants.CORRELATION_ID_HEADER,
                                            correlationId
                                    );
                        }

                        return message;
                    }
            );

            log.info(
                    "event=notification.event_published eventType={} notificationId={} recipientUserId={} correlationId={}",
                    eventType,
                    notification.getId(),
                    notification.getRecipientUserId(),
                    correlationId
            );
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendTask.run();
                }
            });
        } else {
            sendTask.run();
        }
    }

    private String currentCorrelationId() {
        String fromMdc = MDC.get("correlationId");

        if (fromMdc != null && !fromMdc.isBlank()) {
            return fromMdc.trim();
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();

        String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

        return correlationId == null || correlationId.isBlank()
                ? null
                : correlationId.trim();
    }
}