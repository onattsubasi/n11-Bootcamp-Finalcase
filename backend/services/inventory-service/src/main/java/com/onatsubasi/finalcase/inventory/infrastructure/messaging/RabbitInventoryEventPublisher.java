package com.onatsubasi.finalcase.inventory.infrastructure.messaging;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.inventory.application.port.InventoryEventPublisher;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;
import com.onatsubasi.finalcase.inventory.infrastructure.messaging.payload.InventoryItemPayload;
import com.onatsubasi.finalcase.inventory.infrastructure.messaging.payload.StockReservationPayload;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitInventoryEventPublisher implements InventoryEventPublisher {

    private static final String SOURCE = "inventory-service";
    private static final String SOURCE_SERVICE_HEADER = "X-Source-Service";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishStockUpdated(InventoryItem inventoryItem) {
        publishAfterCommit(
                InventoryEventTypes.STOCK_UPDATED,
                InventoryItemPayload.from(inventoryItem),
                inventoryItem.getProductId().toString()
        );
    }

    @Override
    public void publishStockLow(InventoryItem inventoryItem) {
        publishAfterCommit(
                InventoryEventTypes.STOCK_LOW,
                InventoryItemPayload.from(inventoryItem),
                inventoryItem.getProductId().toString()
        );
    }

    @Override
    public void publishOutOfStock(InventoryItem inventoryItem) {
        publishAfterCommit(
                InventoryEventTypes.OUT_OF_STOCK,
                InventoryItemPayload.from(inventoryItem),
                inventoryItem.getProductId().toString()
        );
    }

    @Override
    public void publishBackInStock(InventoryItem inventoryItem) {
        publishAfterCommit(
                InventoryEventTypes.STOCK_BACK_IN_STOCK,
                InventoryItemPayload.from(inventoryItem),
                inventoryItem.getProductId().toString()
        );
    }

    @Override
    public void publishStockReserved(StockReservation reservation) {
        publishAfterCommit(
                InventoryEventTypes.STOCK_RESERVED,
                StockReservationPayload.from(reservation),
                reservation.getId().toString()
        );
    }

    @Override
    public void publishReservationConfirmed(StockReservation reservation) {
        publishAfterCommit(
                InventoryEventTypes.RESERVATION_CONFIRMED,
                StockReservationPayload.from(reservation),
                reservation.getId().toString()
        );
    }

    @Override
    public void publishReservationReleased(StockReservation reservation) {
        publishAfterCommit(
                InventoryEventTypes.RESERVATION_RELEASED,
                StockReservationPayload.from(reservation),
                reservation.getId().toString()
        );
    }

    @Override
    public void publishReservationExpired(StockReservation reservation) {
        publishAfterCommit(
                InventoryEventTypes.RESERVATION_EXPIRED,
                StockReservationPayload.from(reservation),
                reservation.getId().toString()
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
                    "Inventory event registered for after-commit publishing, eventType={}, aggregateId={}, correlationId={}",
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
                    "Inventory event published, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to publish inventory event, eventId={}, eventType={}, aggregateId={}, correlationId={}",
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