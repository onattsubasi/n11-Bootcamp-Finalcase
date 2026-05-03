package com.onatsubasi.finalcase.shipment.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.shipment.application.dto.event.ShipmentChangedEvent;
import com.onatsubasi.finalcase.shipment.infrastructure.mapper.ShipmentMapper;
import com.onatsubasi.finalcase.shipment.infrastructure.messaging.RabbitShipmentEventPublisher;
import com.onatsubasi.finalcase.shipment.infrastructure.messaging.ShipmentEventTypes;
import com.onatsubasi.finalcase.shipment.support.ShipmentTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitShipmentEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishesShipmentEventToMarketplaceExchange() {
        RabbitShipmentEventPublisher publisher = new RabbitShipmentEventPublisher(
                rabbitTemplate,
                new ShipmentMapper(new ObjectMapper())
        );

        publisher.publishShipmentCreated(ShipmentTestData.shipment());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<EventEnvelope<ShipmentChangedEvent>> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);

        verify(rabbitTemplate).convertAndSend(
                eq(EventBrokerConstants.MAIN_EXCHANGE),
                eq(ShipmentEventTypes.SHIPMENT_CREATED),
                envelopeCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        EventEnvelope<ShipmentChangedEvent> envelope = envelopeCaptor.getValue();
        assertThat(envelope.eventType()).isEqualTo(ShipmentEventTypes.SHIPMENT_CREATED);
        assertThat(envelope.source()).isEqualTo("shipment-service");
        assertThat(envelope.payload().orderId()).isEqualTo(ShipmentTestData.ORDER_ID);
    }
}
