package com.onatsubasi.finalcase.checkout.infrastructure.messaging;

import com.onatsubasi.finalcase.checkout.application.port.CheckoutEventPublisher;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.infrastructure.mapper.CheckoutMapper;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RabbitCheckoutEventPublisher implements CheckoutEventPublisher {

    private static final String SOURCE = "checkout-service";

    private final RabbitTemplate rabbitTemplate;
    private final CheckoutMapper checkoutMapper;

    public RabbitCheckoutEventPublisher(
            RabbitTemplate rabbitTemplate,
            CheckoutMapper checkoutMapper
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.checkoutMapper = checkoutMapper;
    }

    @Override
    public void publishCheckoutSubmitted(CheckoutSession session) {
        publish(
                CheckoutEventTypes.CHECKOUT_SUBMITTED,
                checkoutMapper.toChangedEvent(session)
        );
    }

    @Override
    public void publishCheckoutPaymentPending(CheckoutSession session) {
        publish(
                CheckoutEventTypes.CHECKOUT_PAYMENT_PENDING,
                checkoutMapper.toChangedEvent(session)
        );
    }

    @Override
    public void publishCheckoutCompleted(CheckoutSession session) {
        publish(
                CheckoutEventTypes.CHECKOUT_COMPLETED,
                checkoutMapper.toChangedEvent(session)
        );
    }

    @Override
    public void publishCheckoutFailed(CheckoutSession session) {
        publish(
                CheckoutEventTypes.CHECKOUT_FAILED,
                checkoutMapper.toChangedEvent(session)
        );
    }

    @Override
    public void publishCheckoutCompensated(CheckoutSession session) {
        publish(
                CheckoutEventTypes.CHECKOUT_COMPENSATED,
                checkoutMapper.toChangedEvent(session)
        );
    }

    @Override
    public void publishCheckoutFinalizationFailed(CheckoutSession session) {
        publish(
                CheckoutEventTypes.CHECKOUT_FINALIZATION_FAILED,
                checkoutMapper.toChangedEvent(session)
        );
    }

    @Override
    public void publishCheckoutCompensationFailed(CheckoutSession session) {
        publish(
                CheckoutEventTypes.CHECKOUT_COMPENSATION_FAILED,
                checkoutMapper.toChangedEvent(session)
        );
    }

    @Override
    public void publishCheckoutCancelled(CheckoutSession session) {
        publish(
                CheckoutEventTypes.CHECKOUT_CANCELLED,
                checkoutMapper.toChangedEvent(session)
        );
    }

    private <T> void publish(String eventType, T payload) {
        String correlationId = currentCorrelationId();

        EventEnvelope<T> envelope = EventEnvelope.of(
                eventType,
                SOURCE,
                correlationId,
                payload
        );

        rabbitTemplate.convertAndSend(
                EventBrokerConstants.MAIN_EXCHANGE,
                eventType,
                envelope,
                message -> {
                    message.getMessageProperties()
                            .setHeader(
                                    EventBrokerConstants.EVENT_ID_HEADER,
                                    envelope.eventId()
                            );

                    message.getMessageProperties()
                            .setHeader(
                                    EventBrokerConstants.EVENT_TYPE_HEADER,
                                    envelope.eventType()
                            );

                    if (correlationId != null) {
                        message.getMessageProperties()
                                .setHeader(
                                        EventBrokerConstants.CORRELATION_ID_HEADER,
                                        correlationId
                                );
                    }

                    return message;
                }
        );
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