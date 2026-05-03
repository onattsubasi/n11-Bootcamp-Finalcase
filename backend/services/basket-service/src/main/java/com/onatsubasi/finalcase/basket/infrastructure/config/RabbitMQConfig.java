package com.onatsubasi.finalcase.basket.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

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
public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
}

@Bean
public RabbitTemplate rabbitTemplate(
        ConnectionFactory connectionFactory,
        MessageConverter jsonMessageConverter
) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter);
    return rabbitTemplate;
}
}