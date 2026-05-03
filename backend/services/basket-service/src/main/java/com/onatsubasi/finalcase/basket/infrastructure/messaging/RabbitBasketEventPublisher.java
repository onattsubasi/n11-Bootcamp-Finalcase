package com.onatsubasi.finalcase.basket.infrastructure.messaging;

import com.onatsubasi.finalcase.basket.application.port.BasketEventPublisher;
import com.onatsubasi.finalcase.basket.domain.entity.Basket;
import com.onatsubasi.finalcase.basket.infrastructure.messaging.payload.BasketEventPayload;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
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

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitBasketEventPublisher implements BasketEventPublisher {

    private static final String SOURCE = "basket-service";
    private static final String SOURCE_SERVICE_HEADER = "X-Source-Service";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishBasketCreated(Basket basket) {
        publishAfterCommit(
                BasketEventTypes.BASKET_CREATED,
                BasketEventPayload.from(basket),
                basket.getId().toString()
        );
    }

    @Override
    public void publishItemAdded(Basket basket, UUID productId) {
        publishAfterCommit(
                BasketEventTypes.ITEM_ADDED,
                BasketEventPayload.from(basket, productId),
                basket.getId().toString()
        );
    }

    @Override
    public void publishItemQuantityUpdated(Basket basket, UUID productId) {
        publishAfterCommit(
                BasketEventTypes.ITEM_QUANTITY_UPDATED,
                BasketEventPayload.from(basket, productId),
                basket.getId().toString()
        );
    }

    @Override
    public void publishItemRemoved(Basket basket, UUID productId) {
        publishAfterCommit(
                BasketEventTypes.ITEM_REMOVED,
                BasketEventPayload.from(basket, productId),
                basket.getId().toString()
        );
    }

    @Override
    public void publishBasketCleared(Basket basket) {
        publishAfterCommit(
                BasketEventTypes.BASKET_CLEARED,
                BasketEventPayload.from(basket),
                basket.getId().toString()
        );
    }

    @Override
    public void publishCouponIntentUpdated(Basket basket) {
        publishAfterCommit(
                BasketEventTypes.COUPON_INTENT_UPDATED,
                BasketEventPayload.from(basket),
                basket.getId().toString()
        );
    }

    @Override
    public void publishCouponIntentCleared(Basket basket) {
        publishAfterCommit(
                BasketEventTypes.COUPON_INTENT_CLEARED,
                BasketEventPayload.from(basket),
                basket.getId().toString()
        );
    }

    @Override
    public void publishBasketCheckedOut(Basket basket) {
        publishAfterCommit(
                BasketEventTypes.BASKET_CHECKED_OUT,
                BasketEventPayload.from(basket),
                basket.getId().toString()
        );
    }

    private <T> void publishAfterCommit(
            String eventType,
            T payload,
            String aggregateId
    ) {
        String correlationId = currentCorrelationId();

        EventEnvelope<T> envelope = EventEnvelope.of(
                eventType,
                SOURCE,
                correlationId,
                payload
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishImmediately(envelope, aggregateId);
                }
            });

            log.debug(
                    "Basket event registered for after-commit publishing, eventType={}, aggregateId={}, correlationId={}",
                    eventType,
                    aggregateId,
                    correlationId
            );

            return;
        }

        publishImmediately(envelope, aggregateId);
    }

    private <T> void publishImmediately(
            EventEnvelope<T> envelope,
            String aggregateId
    ) {
        String previousEventName = MDC.get("eventName");

        try {
            MDC.put("eventName", envelope.eventType());

            rabbitTemplate.convertAndSend(
                    EventBrokerConstants.MAIN_EXCHANGE,
                    envelope.eventType(),
                    envelope,
                    message -> {
                        message.getMessageProperties()
                                .setHeader(EventBrokerConstants.EVENT_ID_HEADER, envelope.eventId());
                        message.getMessageProperties()
                                .setHeader(EventBrokerConstants.EVENT_TYPE_HEADER, envelope.eventType());
                        message.getMessageProperties()
                                .setHeader(SOURCE_SERVICE_HEADER, SOURCE);

                        if (envelope.correlationId() != null && !envelope.correlationId().isBlank()) {
                            message.getMessageProperties()
                                    .setHeader(
                                            EventBrokerConstants.CORRELATION_ID_HEADER,
                                            envelope.correlationId()
                                    );
                        }

                        return message;
                    }
            );

            log.info(
                    "Basket event published, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to publish basket event, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId(),
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

    private String currentCorrelationId() {
        String mdcCorrelationId = MDC.get("correlationId");

        if (mdcCorrelationId != null && !mdcCorrelationId.isBlank()) {
            return mdcCorrelationId.trim();
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