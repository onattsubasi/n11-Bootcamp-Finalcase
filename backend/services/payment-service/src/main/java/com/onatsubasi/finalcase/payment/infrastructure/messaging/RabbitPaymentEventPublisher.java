package com.onatsubasi.finalcase.payment.infrastructure.messaging;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.payment.application.port.PaymentEventPublisher;
import com.onatsubasi.finalcase.payment.domain.entity.Payment;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentCancellation;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentRefund;
import com.onatsubasi.finalcase.payment.infrastructure.mapper.PaymentMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitPaymentEventPublisher implements PaymentEventPublisher {

    private static final String SOURCE = "payment-service";

    private final RabbitTemplate rabbitTemplate;
    private final PaymentMapper paymentMapper;

    @Override
    public void publishPaymentSucceeded(Payment payment) {
        publishAfterCommit(
                PaymentEventTypes.PAYMENT_SUCCEEDED,
                paymentMapper.toPaymentResultEvent(payment));
    }

    @Override
    public void publishPaymentFailed(Payment payment) {
        publishAfterCommit(
                PaymentEventTypes.PAYMENT_FAILED,
                paymentMapper.toPaymentResultEvent(payment));
    }

    @Override
    public void publishPaymentCancelled(
            Payment payment,
            PaymentCancellation cancellation) {
        publishAfterCommit(
                PaymentEventTypes.PAYMENT_CANCELLED,
                paymentMapper.toPaymentCancelledEvent(payment, cancellation));
    }

    @Override
    public void publishPaymentRefunded(
            Payment payment,
            PaymentRefund refund) {
        publishAfterCommit(
                PaymentEventTypes.PAYMENT_REFUNDED,
                paymentMapper.toPaymentRefundedEvent(payment, refund));
    }

    private <T> void publishAfterCommit(String eventType, T payload) {
        String correlationId = currentCorrelationId();
        Runnable publishAction = () -> publishNow(eventType, payload, correlationId);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAction.run();
                }
            });
            return;
        }

        publishAction.run();
    }

    private <T> void publishNow(String eventType, T payload, String correlationId) {
        EventEnvelope<T> envelope = EventEnvelope.of(
                eventType,
                SOURCE,
                correlationId,
                payload);

        rabbitTemplate.convertAndSend(
                EventBrokerConstants.MAIN_EXCHANGE,
                eventType,
                envelope,
                message -> {
                    message.getMessageProperties()
                            .setHeader(
                                    EventBrokerConstants.EVENT_ID_HEADER,
                                    envelope.eventId());

                    message.getMessageProperties()
                            .setHeader(
                                    EventBrokerConstants.EVENT_TYPE_HEADER,
                                    envelope.eventType());

                    if (correlationId != null) {
                        message.getMessageProperties()
                                .setHeader(
                                        EventBrokerConstants.CORRELATION_ID_HEADER,
                                        correlationId);
                    }

                    return message;
                });

        log.info(
                "event=payment.event_published eventType={} correlationId={}",
                eventType,
                correlationId);
    }

    private String currentCorrelationId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

        return correlationId == null || correlationId.isBlank()
                ? null
                : correlationId.trim();
    }
}
