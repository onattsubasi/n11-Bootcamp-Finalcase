package com.onatsubasi.finalcase.promotion.infrastructure.messaging;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.promotion.application.port.PromotionEventPublisher;
import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservation;
import com.onatsubasi.finalcase.promotion.infrastructure.messaging.payload.CouponAssignmentPayload;
import com.onatsubasi.finalcase.promotion.infrastructure.messaging.payload.CouponPayload;
import com.onatsubasi.finalcase.promotion.infrastructure.messaging.payload.PromotionPayload;
import com.onatsubasi.finalcase.promotion.infrastructure.messaging.payload.PromotionUsageReservationPayload;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitPromotionEventPublisher implements PromotionEventPublisher {

    private static final String SOURCE = "promotion-service";
    private static final String SOURCE_SERVICE_HEADER = "X-Source-Service";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishPromotionCreated(Promotion promotion) {
        publishAfterCommit(PromotionEventTypes.PROMOTION_CREATED, PromotionPayload.from(promotion), promotion.getId().toString());
    }

    @Override
    public void publishPromotionUpdated(Promotion promotion) {
        publishAfterCommit(PromotionEventTypes.PROMOTION_UPDATED, PromotionPayload.from(promotion), promotion.getId().toString());
    }

    @Override
    public void publishPromotionActivated(Promotion promotion) {
        publishAfterCommit(PromotionEventTypes.PROMOTION_ACTIVATED, PromotionPayload.from(promotion), promotion.getId().toString());
    }

    @Override
    public void publishPromotionPaused(Promotion promotion) {
        publishAfterCommit(PromotionEventTypes.PROMOTION_PAUSED, PromotionPayload.from(promotion), promotion.getId().toString());
    }

    @Override
    public void publishPromotionExpired(Promotion promotion) {
        publishAfterCommit(PromotionEventTypes.PROMOTION_EXPIRED, PromotionPayload.from(promotion), promotion.getId().toString());
    }

    @Override
    public void publishPromotionDeleted(Promotion promotion) {
        publishAfterCommit(PromotionEventTypes.PROMOTION_DELETED, PromotionPayload.from(promotion), promotion.getId().toString());
    }

    @Override
    public void publishCouponCreated(Coupon coupon) {
        publishAfterCommit(PromotionEventTypes.COUPON_CREATED, CouponPayload.from(coupon), coupon.getId().toString());
    }

    @Override
    public void publishCouponUpdated(Coupon coupon) {
        publishAfterCommit(PromotionEventTypes.COUPON_UPDATED, CouponPayload.from(coupon), coupon.getId().toString());
    }

    @Override
    public void publishCouponActivated(Coupon coupon) {
        publishAfterCommit(PromotionEventTypes.COUPON_ACTIVATED, CouponPayload.from(coupon), coupon.getId().toString());
    }

    @Override
    public void publishCouponDeactivated(Coupon coupon) {
        publishAfterCommit(PromotionEventTypes.COUPON_DEACTIVATED, CouponPayload.from(coupon), coupon.getId().toString());
    }

    @Override
    public void publishCouponExpired(Coupon coupon) {
        publishAfterCommit(PromotionEventTypes.COUPON_EXPIRED, CouponPayload.from(coupon), coupon.getId().toString());
    }

    @Override
    public void publishCouponAssigned(CouponAssignment assignment) {
        publishAfterCommit(PromotionEventTypes.COUPON_ASSIGNED, CouponAssignmentPayload.from(assignment), assignment.getId().toString());
    }

    @Override
    public void publishUsageReserved(PromotionUsageReservation reservation) {
        publishAfterCommit(PromotionEventTypes.USAGE_RESERVED, PromotionUsageReservationPayload.from(reservation), reservation.getId().toString());
    }

    @Override
    public void publishUsageRedeemed(PromotionUsageReservation reservation) {
        publishAfterCommit(PromotionEventTypes.USAGE_REDEEMED, PromotionUsageReservationPayload.from(reservation), reservation.getId().toString());
    }

    @Override
    public void publishUsageCancelled(PromotionUsageReservation reservation) {
        publishAfterCommit(PromotionEventTypes.USAGE_CANCELLED, PromotionUsageReservationPayload.from(reservation), reservation.getId().toString());
    }

    @Override
    public void publishUsageExpired(PromotionUsageReservation reservation) {
        publishAfterCommit(PromotionEventTypes.USAGE_EXPIRED, PromotionUsageReservationPayload.from(reservation), reservation.getId().toString());
    }

    private <T> void publishAfterCommit(
            String eventType,
            T payload,
            String aggregateId
    ) {
        String correlationId = currentCorrelationId();

        EventEnvelope<T> envelope = EventEnvelope.of(
                eventType,
                SOURCE,
                correlationId,
                payload
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishImmediately(envelope, aggregateId);
                }
            });

            log.debug(
                    "Promotion event registered for after-commit publishing, eventType={}, aggregateId={}, correlationId={}",
                    eventType,
                    aggregateId,
                    correlationId
            );

            return;
        }

        publishImmediately(envelope, aggregateId);
    }

    private <T> void publishImmediately(
            EventEnvelope<T> envelope,
            String aggregateId
    ) {
        String previousEventName = MDC.get("eventName");

        try {
            MDC.put("eventName", envelope.eventType());

            rabbitTemplate.convertAndSend(
                    EventBrokerConstants.MAIN_EXCHANGE,
                    envelope.eventType(),
                    envelope,
                    message -> {
                        message.getMessageProperties()
                                .setHeader(EventBrokerConstants.EVENT_ID_HEADER, envelope.eventId());
                        message.getMessageProperties()
                                .setHeader(EventBrokerConstants.EVENT_TYPE_HEADER, envelope.eventType());
                        message.getMessageProperties()
                                .setHeader(SOURCE_SERVICE_HEADER, SOURCE);

                        if (envelope.correlationId() != null && !envelope.correlationId().isBlank()) {
                            message.getMessageProperties()
                                    .setHeader(
                                            EventBrokerConstants.CORRELATION_ID_HEADER,
                                            envelope.correlationId()
                                    );
                        }

                        return message;
                    }
            );

            log.info(
                    "Promotion event published, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to publish promotion event, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId(),
                    ex
            );
        } finally {
            if (previousEventName == null) {
                MDC.remove("eventName");
            } else {
                MDC.put("eventName", previousEventName);
            }
        }
    }

    private String currentCorrelationId() {
        String mdcCorrelationId = MDC.get("correlationId");

        if (mdcCorrelationId != null && !mdcCorrelationId.isBlank()) {
            return mdcCorrelationId.trim();
        }

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