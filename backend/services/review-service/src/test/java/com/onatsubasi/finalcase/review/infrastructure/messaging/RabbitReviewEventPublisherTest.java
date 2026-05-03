package com.onatsubasi.finalcase.review.infrastructure.messaging;

import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.testsupport.ReviewTestData;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitReviewEventPublisherTest {

    @Test
    void publishesReviewEventsToMarketplaceExchangeWithRoutingKey() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitReviewEventPublisher publisher = new RabbitReviewEventPublisher(rabbitTemplate);
        Review review = ReviewTestData.approvedReview();

        publisher.publishReviewApproved(review);

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(rabbitTemplate).convertAndSend(
                eq(EventBrokerConstants.MAIN_EXCHANGE),
                eq(ReviewEventTypes.REVIEW_APPROVED),
                envelopeCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        EventEnvelope<?> envelope = envelopeCaptor.getValue();
        assertThat(envelope.eventType()).isEqualTo(ReviewEventTypes.REVIEW_APPROVED);
        assertThat(envelope.source()).isEqualTo("review-service");
        assertThat(envelope.payload()).isNotNull();
    }
}
