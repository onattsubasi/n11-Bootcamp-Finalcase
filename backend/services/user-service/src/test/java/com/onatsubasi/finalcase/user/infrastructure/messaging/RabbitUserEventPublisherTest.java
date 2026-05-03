package com.onatsubasi.finalcase.user.infrastructure.messaging;

import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitUserEventPublisherTest {

    @Test
    @DisplayName("publishes user events through marketplace exchange with event routing key")
    void publishesProfileCreatedEvent() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitUserEventPublisher publisher = new RabbitUserEventPublisher(rabbitTemplate);
        UserProfile profile = UserProfile.createLazy(UUID.randomUUID(), "user@example.com", "tr");

        publisher.publishProfileCreated(profile);

        ArgumentCaptor<Object> envelopeCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                eq(EventBrokerConstants.MAIN_EXCHANGE),
                eq(UserEventTypes.PROFILE_CREATED),
                envelopeCaptor.capture(),
                any(MessagePostProcessor.class)
        );
        assertThat(envelopeCaptor.getValue()).isNotNull();
    }
}
