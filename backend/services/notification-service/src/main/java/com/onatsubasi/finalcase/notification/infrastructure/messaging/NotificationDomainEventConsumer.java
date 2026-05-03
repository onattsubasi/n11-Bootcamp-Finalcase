package com.onatsubasi.finalcase.notification.infrastructure.messaging;

import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.notification.application.dto.command.CreateNotificationCommand;
import com.onatsubasi.finalcase.notification.application.service.NotificationCommandService;
import com.onatsubasi.finalcase.notification.application.service.NotificationEventPayloadMapper;
import com.onatsubasi.finalcase.notification.application.service.NotificationEventProcessingService;
import com.onatsubasi.finalcase.notification.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDomainEventConsumer {

    private final NotificationEventProcessingService eventProcessingService;
    private final NotificationEventPayloadMapper payloadMapper;
    private final NotificationCommandService notificationCommandService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_EVENTS_QUEUE)
    public void handleDomainEvent(EventEnvelope<?> envelope) {
        if (envelope == null) {
            log.warn("event=notification.domain_event_null");
            return;
        }

        String eventId = envelope.eventId();
        String eventType = envelope.eventType();

        if (!eventProcessingService.shouldProcess(eventId)) {
            return;
        }

        try {
            CreateNotificationCommand command = payloadMapper.toCommand(
                    eventType,
                    envelope.payload()
            );

            if (command == null) {
                eventProcessingService.markSkipped(
                        eventId,
                        eventType,
                        "Unsupported or incomplete notification event"
                );

                log.debug(
                        "event=notification.domain_event_skipped eventId={} eventType={}",
                        eventId,
                        eventType
                );

                return;
            }

            notificationCommandService.createNotification(command);

            eventProcessingService.markProcessed(eventId, eventType);

            log.info(
                    "event=notification.domain_event_processed eventId={} eventType={}",
                    eventId,
                    eventType
            );
        } catch (Exception ex) {
            eventProcessingService.markFailed(
                    eventId,
                    eventType,
                    ex.getMessage()
            );

            log.error(
                    "event=notification.domain_event_failed eventId={} eventType={}",
                    eventId,
                    eventType,
                    ex
            );

            throw ex;
        }
    }
}