package com.onatsubasi.finalcase.order.infrastructure.messaging;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.order.application.port.OrderEventPublisher;
import com.onatsubasi.finalcase.order.domain.entity.Order;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitOrderEventPublisher implements OrderEventPublisher {

    private static final String SOURCE = "order-service";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishOrderCreated(Order order) {
        publishAfterCommit(OrderEventTypes.ORDER_CREATED, OrderChangedEvent.from(order));
    }

    @Override
    public void publishOrderPaid(Order order) {
        publishAfterCommit(OrderEventTypes.ORDER_PAID, OrderChangedEvent.from(order));
    }

    @Override
    public void publishOrderPaymentFailed(Order order) {
        publishAfterCommit(OrderEventTypes.ORDER_PAYMENT_FAILED, OrderChangedEvent.from(order));
    }

    @Override
    public void publishOrderCancelled(Order order) {
        publishAfterCommit(OrderEventTypes.ORDER_CANCELLED, OrderChangedEvent.from(order));
    }

    @Override
    public void publishOrderPreparing(Order order) {
        publishAfterCommit(OrderEventTypes.ORDER_PREPARING, OrderChangedEvent.from(order));
    }

    @Override
    public void publishOrderShipped(Order order) {
        publishAfterCommit(OrderEventTypes.ORDER_SHIPPED, OrderChangedEvent.from(order));
    }

    @Override
    public void publishOrderDelivered(Order order) {
        publishAfterCommit(OrderEventTypes.ORDER_DELIVERED, OrderChangedEvent.from(order));
    }

    private <T> void publishAfterCommit(String eventType, T payload) {
        String correlationId = currentCorrelationId();
        EventEnvelope<T> envelope = EventEnvelope.of(eventType, SOURCE, correlationId, payload);

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send(eventType, correlationId, envelope);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                send(eventType, correlationId, envelope);
            }
        });
    }

    private <T> void send(String eventType, String correlationId, EventEnvelope<T> envelope) {
        rabbitTemplate.convertAndSend(
                EventBrokerConstants.MAIN_EXCHANGE,
                eventType,
                envelope,
                message -> {
                    message.getMessageProperties().setHeader(EventBrokerConstants.EVENT_ID_HEADER, envelope.eventId());
                    message.getMessageProperties().setHeader(EventBrokerConstants.EVENT_TYPE_HEADER, envelope.eventType());
                    if (correlationId != null) {
                        message.getMessageProperties().setHeader(EventBrokerConstants.CORRELATION_ID_HEADER, correlationId);
                    }
                    return message;
                }
        );
        log.info("Published order event eventType={} eventId={} correlationId={}", eventType, envelope.eventId(), correlationId);
    }

    private String currentCorrelationId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);
        return correlationId == null || correlationId.isBlank() ? null : correlationId.trim();
    }
}
