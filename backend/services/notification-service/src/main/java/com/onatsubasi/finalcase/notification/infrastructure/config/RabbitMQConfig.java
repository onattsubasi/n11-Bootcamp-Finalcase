package com.onatsubasi.finalcase.notification.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.notification.infrastructure.messaging.NotificationEventTypes;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_EVENTS_QUEUE =
            "notification-service.domain-events.queue";

    public static final String NOTIFICATION_EVENTS_DLQ =
            "notification-service.domain-events.dlq";

    public static final String NOTIFICATION_EVENTS_DLQ_ROUTING_KEY =
            "notification-service.domain-events.dlq";

    @Bean
    public TopicExchange marketplaceEventsExchange() {
        return ExchangeBuilder
                .topicExchange(EventBrokerConstants.MAIN_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange marketplaceDeadLetterExchange() {
        return ExchangeBuilder
                .topicExchange(EventBrokerConstants.DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue notificationEventsQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", EventBrokerConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_EVENTS_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue notificationEventsDeadLetterQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_EVENTS_DLQ)
                .build();
    }

    @Bean
    public Binding notificationEventsDeadLetterBinding(
            Queue notificationEventsDeadLetterQueue,
            TopicExchange marketplaceDeadLetterExchange
    ) {
        return BindingBuilder
                .bind(notificationEventsDeadLetterQueue)
                .to(marketplaceDeadLetterExchange)
                .with(NOTIFICATION_EVENTS_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding orderCreatedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.ORDER_CREATED);
    }

    @Bean
    public Binding orderPaidBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.ORDER_PAID);
    }

    @Bean
    public Binding orderPaymentFailedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.ORDER_PAYMENT_FAILED);
    }

    @Bean
    public Binding orderCancelledBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.ORDER_CANCELLED);
    }

    @Bean
    public Binding paymentSucceededBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.PAYMENT_SUCCEEDED);
    }

    @Bean
    public Binding paymentFailedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.PAYMENT_FAILED);
    }

    @Bean
    public Binding paymentRefundedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.PAYMENT_REFUNDED);
    }

    @Bean
    public Binding shipmentCreatedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.SHIPMENT_CREATED);
    }

    @Bean
    public Binding shipmentShippedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.SHIPMENT_SHIPPED);
    }

    @Bean
    public Binding shipmentDeliveredBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.SHIPMENT_DELIVERED);
    }

    @Bean
    public Binding shipmentDeliveryFailedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.SHIPMENT_DELIVERY_FAILED);
    }

    @Bean
    public Binding shipmentCancelledBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.SHIPMENT_CANCELLED);
    }

    @Bean
    public Binding checkoutCompletedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.CHECKOUT_COMPLETED);
    }

    @Bean
    public Binding checkoutFailedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.CHECKOUT_FAILED);
    }

    @Bean
    public Binding checkoutFinalizationFailedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.CHECKOUT_FINALIZATION_FAILED);
    }

    @Bean
    public Binding inventoryLowStockBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.INVENTORY_LOW_STOCK);
    }

    @Bean
    public Binding inventoryOutOfStockBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.INVENTORY_OUT_OF_STOCK);
    }

    @Bean
    public Binding inventoryBackInStockBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.INVENTORY_BACK_IN_STOCK);
    }

    @Bean
    public Binding promotionAvailableBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.PROMOTION_AVAILABLE);
    }

    @Bean
    public Binding promotionExpiringSoonBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.PROMOTION_EXPIRING_SOON);
    }

    @Bean
    public Binding couponAssignedBinding(
            Queue notificationEventsQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return bind(notificationEventsQueue, marketplaceEventsExchange, NotificationEventTypes.COUPON_ASSIGNED);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(
            ObjectMapper objectMapper
    ) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);

        return factory;
    }

    private Binding bind(
            Queue queue,
            TopicExchange exchange,
            String routingKey
    ) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(routingKey);
    }
}