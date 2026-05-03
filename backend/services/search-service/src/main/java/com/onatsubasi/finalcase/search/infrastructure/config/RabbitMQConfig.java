package com.onatsubasi.finalcase.search.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String SEARCH_PROJECTION_QUEUE = "search.projection.queue";
    public static final String SEARCH_PROJECTION_DLQ = "search.projection.dlq";

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
    public Queue searchProjectionQueue() {
        return QueueBuilder
                .durable(SEARCH_PROJECTION_QUEUE)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", EventBrokerConstants.DEAD_LETTER_EXCHANGE,
                        "x-dead-letter-routing-key", SEARCH_PROJECTION_DLQ
                ))
                .build();
    }

    @Bean
    public Queue searchProjectionDeadLetterQueue() {
        return QueueBuilder
                .durable(SEARCH_PROJECTION_DLQ)
                .build();
    }

    @Bean
    public Binding catalogProjectionBinding(Queue searchProjectionQueue, TopicExchange marketplaceEventsExchange) {
        return BindingBuilder
                .bind(searchProjectionQueue)
                .to(marketplaceEventsExchange)
                .with("catalog.#");
    }

    @Bean
    public Binding inventoryProjectionBinding(Queue searchProjectionQueue, TopicExchange marketplaceEventsExchange) {
        return BindingBuilder
                .bind(searchProjectionQueue)
                .to(marketplaceEventsExchange)
                .with("inventory.#");
    }

    @Bean
    public Binding promotionProjectionBinding(Queue searchProjectionQueue, TopicExchange marketplaceEventsExchange) {
        return BindingBuilder
                .bind(searchProjectionQueue)
                .to(marketplaceEventsExchange)
                .with("promotion.#");
    }

    @Bean
    public Binding reviewProjectionBinding(Queue searchProjectionQueue, TopicExchange marketplaceEventsExchange) {
        return BindingBuilder
                .bind(searchProjectionQueue)
                .to(marketplaceEventsExchange)
                .with("review.#");
    }

    @Bean
    public Binding searchProjectionDlqBinding(
            Queue searchProjectionDeadLetterQueue,
            TopicExchange marketplaceDeadLetterExchange
    ) {
        return BindingBuilder
                .bind(searchProjectionDeadLetterQueue)
                .to(marketplaceDeadLetterExchange)
                .with(SEARCH_PROJECTION_DLQ);
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
