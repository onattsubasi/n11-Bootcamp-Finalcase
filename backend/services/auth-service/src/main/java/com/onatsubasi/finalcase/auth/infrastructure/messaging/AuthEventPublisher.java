package com.onatsubasi.finalcase.auth.infrastructure.messaging;

import com.onatsubasi.finalcase.auth.domain.event.UserRegisteredIntegrationEvent;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventPublisher {

    private static final String SOURCE = "auth-service";

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegisteredAfterCommit(
            UUID userId,
            String email,
            Set<String> roles
    ) {
        UserRegisteredIntegrationEvent payload = new UserRegisteredIntegrationEvent(
                userId,
                email,
                roles
        );

        EventEnvelope<UserRegisteredIntegrationEvent> envelope = EventEnvelope.of(
                AuthEventTypes.USER_REGISTERED,
                SOURCE,
                MDC.get("correlationId"),
                payload
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishSafely(envelope, userId);
                }
            });
            return;
        }

        publishSafely(envelope, userId);
    }

    private void publishSafely(
            EventEnvelope<UserRegisteredIntegrationEvent> envelope,
            UUID userId
    ) {
        String previousEventName = MDC.get("eventName");

        try {
            MDC.put("eventName", AuthEventTypes.USER_REGISTERED);

            rabbitTemplate.convertAndSend(
                    EventBrokerConstants.MAIN_EXCHANGE,
                    AuthEventTypes.USER_REGISTERED,
                    envelope
            );

            log.info(
                    "Auth event published: eventType={}, eventId={}, userId={}",
                    AuthEventTypes.USER_REGISTERED,
                    envelope.eventId(),
                    userId
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to publish auth event: eventType={}, userId={}",
                    AuthEventTypes.USER_REGISTERED,
                    userId,
                    ex
            );
        } finally {
            if (previousEventName == null) {
                MDC.remove("eventName");
            } else {
                MDC.put("eventName", previousEventName);
            }
        }
    }
}
