package com.onatsubasi.finalcase.checkout.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.application.dto.event.PaymentResultEventMessage;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutCompensationService;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutFinalizationService;
import com.onatsubasi.finalcase.checkout.domain.repository.ProcessedPaymentEventRepository;
import com.onatsubasi.finalcase.checkout.support.CheckoutTestFixtures;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentResultEventConsumerTest {

    @Mock
    private CheckoutFinalizationService finalizationService;

    @Mock
    private CheckoutCompensationService compensationService;

    @Mock
    private ProcessedPaymentEventRepository processedPaymentEventRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PaymentResultEventConsumer consumer;

    @Test
    void handlePaymentResult_dispatchesPaymentSucceededToFinalization() {
        String eventId = UUID.randomUUID().toString();
        PaymentResultEventMessage payload = CheckoutTestFixtures.paymentSucceededEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        EventEnvelope<PaymentResultEventMessage> envelope = envelope(eventId, CheckoutEventTypes.PAYMENT_SUCCEEDED, payload);

        consumer.handlePaymentResult(envelope);

        verify(finalizationService).finalizePaymentSuccess(eventId, CheckoutEventTypes.PAYMENT_SUCCEEDED, payload);
        verify(compensationService, never()).compensatePaymentFailure(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handlePaymentResult_dispatchesPaymentFailedToCompensation() {
        String eventId = UUID.randomUUID().toString();
        PaymentResultEventMessage payload = CheckoutTestFixtures.paymentFailedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        EventEnvelope<PaymentResultEventMessage> envelope = envelope(eventId, CheckoutEventTypes.PAYMENT_FAILED, payload);

        consumer.handlePaymentResult(envelope);

        verify(compensationService).compensatePaymentFailure(eventId, CheckoutEventTypes.PAYMENT_FAILED, payload);
        verify(finalizationService, never()).finalizePaymentSuccess(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handlePaymentResult_ignoresAlreadyProcessedEvent() {
        String eventId = UUID.randomUUID().toString();
        when(processedPaymentEventRepository.existsByEventId(eventId)).thenReturn(true);
        EventEnvelope<PaymentResultEventMessage> envelope = envelope(
                eventId,
                CheckoutEventTypes.PAYMENT_SUCCEEDED,
                CheckoutTestFixtures.paymentSucceededEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        );

        consumer.handlePaymentResult(envelope);

        verify(finalizationService, never()).finalizePaymentSuccess(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(compensationService, never()).compensatePaymentFailure(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private static EventEnvelope<PaymentResultEventMessage> envelope(
            String eventId,
            String eventType,
            PaymentResultEventMessage payload
    ) {
        return new EventEnvelope<>(
                eventId,
                eventType,
                1,
                "payment-service",
                UUID.randomUUID().toString(),
                Instant.now(),
                payload,
                Map.of()
        );
    }
}
