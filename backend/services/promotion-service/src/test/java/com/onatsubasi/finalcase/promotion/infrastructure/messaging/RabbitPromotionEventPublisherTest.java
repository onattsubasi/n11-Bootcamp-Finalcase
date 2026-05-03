package com.onatsubasi.finalcase.promotion.infrastructure.messaging;

import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.promotion.TestDataFactory;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitPromotionEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void publishPromotionCreatedRegistersAfterCommitWhenTransactionIsActive() {
        RabbitPromotionEventPublisher publisher = new RabbitPromotionEventPublisher(rabbitTemplate);
        Promotion promotion = TestDataFactory.activePromotion(
                UUID.randomUUID(),
                PromotionType.PERCENTAGE_DISCOUNT,
                TestDataFactory.percentageConfig("10")
        );

        TransactionSynchronizationManager.initSynchronization();
        publisher.publishPromotionCreated(promotion);

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(), any(MessagePostProcessor.class));
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertThat(synchronizations).hasSize(1);

        synchronizations.forEach(TransactionSynchronization::afterCommit);

        ArgumentCaptor<EventEnvelope<?>> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(rabbitTemplate).convertAndSend(
                eq(EventBrokerConstants.MAIN_EXCHANGE),
                eq(PromotionEventTypes.PROMOTION_CREATED),
                envelopeCaptor.capture(),
                any(MessagePostProcessor.class)
        );
        assertThat(envelopeCaptor.getValue().eventType()).isEqualTo(PromotionEventTypes.PROMOTION_CREATED);
        assertThat(envelopeCaptor.getValue().source()).isEqualTo("promotion-service");
    }
}
