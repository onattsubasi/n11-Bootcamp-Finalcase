package com.onatsubasi.finalcase.checkout.application.service;

import com.onatsubasi.finalcase.checkout.application.dto.client.ShipmentClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.event.PaymentResultEventMessage;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSessionResponse;
import com.onatsubasi.finalcase.checkout.application.port.CheckoutEventPublisher;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.domain.entity.ProcessedPaymentEvent;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepName;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.checkout.domain.repository.CheckoutSessionRepository;
import com.onatsubasi.finalcase.checkout.domain.repository.ProcessedPaymentEventRepository;
import com.onatsubasi.finalcase.checkout.infrastructure.mapper.CheckoutMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutFinalizationService {

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;
    private final CheckoutDownstreamGateway downstreamGateway;
    private final CheckoutMapper checkoutMapper;
    private final CheckoutEventPublisher eventPublisher;

    @Transactional
    public void finalizePaymentSuccess(
            String eventId,
            String eventType,
            PaymentResultEventMessage event
    ) {
        log.info(
                "event=checkout.payment_succeeded_received eventId={} paymentId={} checkoutId={} orderId={}",
                eventId,
                event.paymentId(),
                event.checkoutId(),
                event.orderId()
        );

        if (eventId != null && processedPaymentEventRepository.existsByEventId(eventId)) {
            log.info(
                    "event=checkout.payment_event_duplicate eventId={} eventType={}",
                    eventId,
                    eventType
            );
            return;
        }

        CheckoutSession session = checkoutSessionRepository
                .findByPaymentIdForUpdate(event.paymentId())
                .orElseThrow(() -> new BaseException(
                        CheckoutErrorCode.CHECKOUT_SESSION_NOT_FOUND
                ));

        if (session.getStatus() == CheckoutStatus.COMPLETED) {
            markProcessed(eventId, eventType, event, session);
            return;
        }

        try {
            log.info(
                    "event=checkout.finalization_started checkoutId={} orderId={} paymentId={}",
                    session.getId(),
                    session.getOrderId(),
                    session.getPaymentId()
            );

            session.completeStep(CheckoutSagaStepName.PAYMENT_SUCCEEDED_RECEIVED);

            if (!session.isStepCompleted(CheckoutSagaStepName.INVENTORY_CONFIRMED)
                    && session.getInventoryReservationId() != null) {
                downstreamGateway.confirmReservation(session.getInventoryReservationId());
                session.completeStep(CheckoutSagaStepName.INVENTORY_CONFIRMED);
            }

            if (!session.isStepCompleted(CheckoutSagaStepName.PROMOTION_REDEEMED)
                    && session.getOrderId() != null
                    && session.getPromotionUsageReservationId() != null) {
                downstreamGateway.redeemPromotionUsage(session.getOrderId());
                session.completeStep(CheckoutSagaStepName.PROMOTION_REDEEMED);
            }

            if (!session.isStepCompleted(CheckoutSagaStepName.ORDER_MARKED_PAID)
                    && session.getOrderId() != null) {
                downstreamGateway.markOrderPaid(
                        session.getOrderId(),
                        checkoutMapper.toMarkOrderPaidRequest(event)
                );
                session.completeStep(CheckoutSagaStepName.ORDER_MARKED_PAID);
            }

            if (!session.isStepCompleted(CheckoutSagaStepName.BASKET_MARKED_CHECKED_OUT)
                    && session.getBasketId() != null
                    && session.getOrderId() != null) {
                downstreamGateway.markBasketCheckedOut(
                        session.getBasketId(),
                        session.getId(),
                        session.getOrderId()
                );
                session.completeStep(CheckoutSagaStepName.BASKET_MARKED_CHECKED_OUT);
            }

            if (!session.isStepCompleted(CheckoutSagaStepName.SHIPMENT_CREATED)
                    && session.getOrderId() != null) {
                ShipmentClientResponse shipment =
                        downstreamGateway.createShipmentForOrder(session.getOrderId());

                session.markCompleted(shipment.shipmentId());
                session.completeStep(CheckoutSagaStepName.SHIPMENT_CREATED);
            } else {
                session.markCompleted(session.getShipmentId());
            }

            session.completeStep(CheckoutSagaStepName.CHECKOUT_COMPLETED);

            CheckoutSession saved = checkoutSessionRepository.save(session);

            markProcessed(eventId, eventType, event, saved);

            eventPublisher.publishCheckoutCompleted(saved);

            log.info(
                    "event=checkout.finalization_completed checkoutId={} orderId={} paymentId={} shipmentId={}",
                    saved.getId(),
                    saved.getOrderId(),
                    saved.getPaymentId(),
                    saved.getShipmentId()
            );
        } catch (Exception ex) {
            session.markFinalizationFailed();
            session.failStep(
                    CheckoutSagaStepName.FINALIZATION_FAILED,
                    ex.getMessage()
            );

            CheckoutSession saved = checkoutSessionRepository.save(session);

            eventPublisher.publishCheckoutFinalizationFailed(saved);

            log.error(
                    "event=checkout.finalization_failed checkoutId={} orderId={} paymentId={}",
                    saved.getId(),
                    saved.getOrderId(),
                    saved.getPaymentId(),
                    ex
            );

            throw ex;
        }
    }

    private void markProcessed(
            String eventId,
            String eventType,
            PaymentResultEventMessage event,
            CheckoutSession session
    ) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }

        processedPaymentEventRepository.save(
                new ProcessedPaymentEvent(
                        eventId,
                        eventType,
                        event.paymentId(),
                        session.getId()
                )
        );
    }

    @Transactional
    public CheckoutSessionResponse retryFinalization(UUID checkoutId) {
        CheckoutSession session = checkoutSessionRepository.findByIdForUpdate(checkoutId)
                .orElseThrow(() -> new BaseException(
                        CheckoutErrorCode.CHECKOUT_SESSION_NOT_FOUND
                ));

        if (session.getStatus() != CheckoutStatus.FINALIZATION_FAILED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_INVALID_STATUS);
        }

        log.warn(
                "event=checkout.finalization_retry_requested checkoutId={} orderId={} paymentId={}",
                session.getId(),
                session.getOrderId(),
                session.getPaymentId()
        );

        session.completeStep(CheckoutSagaStepName.FINALIZATION_RETRY_REQUESTED);
        checkoutSessionRepository.save(session);

        PaymentResultEventMessage syntheticEvent = new PaymentResultEventMessage(
                session.getPaymentId(),
                session.getId(),
                session.getOrderId(),
                session.getUserId(),
                null,
                "SUCCEEDED",
                null,
                session.getGrandTotalAmount(),
                session.getCurrency(),
                null
        );

        finalizePaymentSuccess(null, "manual.retry.finalization", syntheticEvent);

        CheckoutSession refreshed = checkoutSessionRepository.findById(checkoutId)
                .orElseThrow(() -> new BaseException(
                        CheckoutErrorCode.CHECKOUT_SESSION_NOT_FOUND
                ));

        return checkoutMapper.toSessionResponse(refreshed);
    }
}
