package com.onatsubasi.finalcase.checkout.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.application.dto.event.PaymentResultEventMessage;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutCompensationService;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutFinalizationService;
import com.onatsubasi.finalcase.checkout.domain.repository.ProcessedPaymentEventRepository;
import com.onatsubasi.finalcase.checkout.infrastructure.config.RabbitMQConfig;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentResultEventConsumer {

    private final CheckoutFinalizationService finalizationService;
    private final CheckoutCompensationService compensationService;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_RESULT_QUEUE)
    public void handlePaymentResult(EventEnvelope<?> envelope) {
        if (envelope == null) {
            log.warn("event=checkout.payment_event_null");
            return;
        }

        log.info(
                "event=checkout.payment_event_received eventId={} eventType={}",
                envelope.eventId(),
                envelope.eventType()
        );

        if (envelope.eventId() != null
                && processedPaymentEventRepository.existsByEventId(envelope.eventId())) {
            log.info(
                    "event=checkout.payment_event_duplicate eventId={} eventType={}",
                    envelope.eventId(),
                    envelope.eventType()
            );
            return;
        }

        PaymentResultEventMessage event = objectMapper.convertValue(
                envelope.payload(),
                PaymentResultEventMessage.class
        );

        if (CheckoutEventTypes.PAYMENT_SUCCEEDED.equals(envelope.eventType())) {
            finalizationService.finalizePaymentSuccess(
                    envelope.eventId(),
                    envelope.eventType(),
                    event
            );
            return;
        }

        if (CheckoutEventTypes.PAYMENT_FAILED.equals(envelope.eventType())) {
            compensationService.compensatePaymentFailure(
                    envelope.eventId(),
                    envelope.eventType(),
                    event
            );
            return;
        }

        log.debug(
                "event=checkout.payment_event_ignored eventId={} eventType={}",
                envelope.eventId(),
                envelope.eventType()
        );
    }
}