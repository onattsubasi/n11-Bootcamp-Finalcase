package com.onatsubasi.finalcase.common.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({RabbitTemplate.class, Jackson2JsonMessageConverter.class})
public class EventRabbitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter eventJsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplateCustomizer eventRabbitTemplateCustomizer(MessageConverter eventJsonMessageConverter) {
        return rabbitTemplate -> rabbitTemplate.setMessageConverter(eventJsonMessageConverter);
    }

    @Bean
    public Declarables marketplaceEventExchanges() {
        TopicExchange mainExchange = ExchangeBuilder
                .topicExchange(EventBrokerConstants.MAIN_EXCHANGE)
                .durable(true)
                .build();

        TopicExchange deadLetterExchange = ExchangeBuilder
                .topicExchange(EventBrokerConstants.DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();

        TopicExchange retryExchange = ExchangeBuilder
                .topicExchange(EventBrokerConstants.RETRY_EXCHANGE)
                .durable(true)
                .build();

        return new Declarables(mainExchange, deadLetterExchange, retryExchange);
    }
}
