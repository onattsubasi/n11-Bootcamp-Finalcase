package com.onatsubasi.finalcase.shipment.infrastructure.messaging;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.shipment.application.dto.event.ShipmentChangedEvent;
import com.onatsubasi.finalcase.shipment.application.port.ShipmentEventPublisher;
import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.infrastructure.mapper.ShipmentMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class RabbitShipmentEventPublisher implements ShipmentEventPublisher {

    private static final String SOURCE = "shipment-service";

    private final RabbitTemplate rabbitTemplate;
    private final ShipmentMapper shipmentMapper;

    @Override
    public void publishShipmentCreated(Shipment shipment) {
        publish(ShipmentEventTypes.SHIPMENT_CREATED, shipment);
    }

    @Override
    public void publishShipmentReadyToShip(Shipment shipment) {
        publish(ShipmentEventTypes.SHIPMENT_READY_TO_SHIP, shipment);
    }

    @Override
    public void publishShipmentShipped(Shipment shipment) {
        publish(ShipmentEventTypes.SHIPMENT_SHIPPED, shipment);
    }

    @Override
    public void publishShipmentInTransit(Shipment shipment) {
        publish(ShipmentEventTypes.SHIPMENT_IN_TRANSIT, shipment);
    }

    @Override
    public void publishShipmentOutForDelivery(Shipment shipment) {
        publish(ShipmentEventTypes.SHIPMENT_OUT_FOR_DELIVERY, shipment);
    }

    @Override
    public void publishShipmentDelivered(Shipment shipment) {
        publish(ShipmentEventTypes.SHIPMENT_DELIVERED, shipment);
    }

    @Override
    public void publishShipmentDeliveryFailed(Shipment shipment) {
        publish(ShipmentEventTypes.SHIPMENT_DELIVERY_FAILED, shipment);
    }

    @Override
    public void publishShipmentCancelled(Shipment shipment) {
        publish(ShipmentEventTypes.SHIPMENT_CANCELLED, shipment);
    }

    private void publish(String eventType, Shipment shipment) {
        String correlationId = currentCorrelationId();

        EventEnvelope<ShipmentChangedEvent> envelope = EventEnvelope.of(
                eventType,
                SOURCE,
                correlationId,
                shipmentMapper.toChangedEvent(shipment)
        );

        Runnable sendAction = () -> send(eventType, shipment, envelope, correlationId);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendAction.run();
                }
            });
            return;
        }

        sendAction.run();
    }

    private void send(
            String eventType,
            Shipment shipment,
            EventEnvelope<ShipmentChangedEvent> envelope,
            String correlationId
    ) {
        rabbitTemplate.convertAndSend(
                EventBrokerConstants.MAIN_EXCHANGE,
                eventType,
                envelope,
                message -> {
                    message.getMessageProperties()
                            .setHeader(EventBrokerConstants.EVENT_ID_HEADER, envelope.eventId());

                    message.getMessageProperties()
                            .setHeader(EventBrokerConstants.EVENT_TYPE_HEADER, envelope.eventType());

                    if (correlationId != null) {
                        message.getMessageProperties()
                                .setHeader(EventBrokerConstants.CORRELATION_ID_HEADER, correlationId);
                    }

                    return message;
                }
        );

        log.info(
                "event=shipment.event_published eventType={} shipmentId={} orderId={} status={} correlationId={}",
                eventType,
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getStatus(),
                correlationId
        );
    }

    private String currentCorrelationId() {
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
