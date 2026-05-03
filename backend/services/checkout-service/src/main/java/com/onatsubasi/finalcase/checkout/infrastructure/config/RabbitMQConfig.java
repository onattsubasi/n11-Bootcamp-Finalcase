package com.onatsubasi.finalcase.checkout.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.infrastructure.messaging.CheckoutEventTypes;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_RESULT_QUEUE =
            "checkout-service.payment-result.queue";

    public static final String PAYMENT_RESULT_DLQ =
            "checkout-service.payment-result.dlq";

    public static final String PAYMENT_RESULT_DLQ_ROUTING_KEY =
            "checkout-service.payment-result.dlq";

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
    public Queue paymentResultQueue() {
        return QueueBuilder
                .durable(PAYMENT_RESULT_QUEUE)
                .withArgument("x-dead-letter-exchange", EventBrokerConstants.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PAYMENT_RESULT_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue paymentResultDeadLetterQueue() {
        return QueueBuilder
                .durable(PAYMENT_RESULT_DLQ)
                .build();
    }

    @Bean
    public Binding paymentSucceededBinding(
            Queue paymentResultQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return BindingBuilder
                .bind(paymentResultQueue)
                .to(marketplaceEventsExchange)
                .with(CheckoutEventTypes.PAYMENT_SUCCEEDED);
    }

    @Bean
    public Binding paymentFailedBinding(
            Queue paymentResultQueue,
            TopicExchange marketplaceEventsExchange
    ) {
        return BindingBuilder
                .bind(paymentResultQueue)
                .to(marketplaceEventsExchange)
                .with(CheckoutEventTypes.PAYMENT_FAILED);
    }

    @Bean
    public Binding paymentResultDeadLetterBinding(
            Queue paymentResultDeadLetterQueue,
            TopicExchange marketplaceDeadLetterExchange
    ) {
        return BindingBuilder
                .bind(paymentResultDeadLetterQueue)
                .to(marketplaceDeadLetterExchange)
                .with(PAYMENT_RESULT_DLQ_ROUTING_KEY);
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
}