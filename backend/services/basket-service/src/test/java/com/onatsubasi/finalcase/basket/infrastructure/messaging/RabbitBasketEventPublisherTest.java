package com.onatsubasi.finalcase.basket.infrastructure.messaging;

import com.onatsubasi.finalcase.basket.domain.entity.Basket;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitBasketEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitBasketEventPublisher publisher;

    @Test
    @DisplayName("publishItemAdded sends basket event envelope to marketplace exchange")
    void shouldPublishItemAddedEvent() {
        Basket basket = Basket.empty(UUID.randomUUID());
        UUID productId = UUID.randomUUID();
        basket.addItem(productId, 2);

        publisher.publishItemAdded(basket, productId);

        ArgumentCaptor<EventEnvelope<?>> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(rabbitTemplate).convertAndSend(
                eq(EventBrokerConstants.MAIN_EXCHANGE),
                eq(BasketEventTypes.ITEM_ADDED),
                envelopeCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        EventEnvelope<?> envelope = envelopeCaptor.getValue();
        assertThat(envelope.eventType()).isEqualTo(BasketEventTypes.ITEM_ADDED);
        assertThat(envelope.source()).isEqualTo("basket-service");
        assertThat(envelope.eventId()).isNotBlank();
        assertThat(envelope.payload()).isNotNull();
    }

    @Test
    @DisplayName("publishBasketCheckedOut uses checked-out routing key")
    void shouldPublishBasketCheckedOutEvent() {
        Basket basket = Basket.empty(UUID.randomUUID());
        basket.addItem(UUID.randomUUID(), 1);
        basket.markCheckedOut(UUID.randomUUID());

        publisher.publishBasketCheckedOut(basket);

        verify(rabbitTemplate).convertAndSend(
                eq(EventBrokerConstants.MAIN_EXCHANGE),
                eq(BasketEventTypes.BASKET_CHECKED_OUT),
                any(EventEnvelope.class),
                any(MessagePostProcessor.class)
        );
    }
}
