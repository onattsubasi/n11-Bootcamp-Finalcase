package com.onatsubasi.finalcase.checkout.application.service;

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
public class CheckoutCompensationService {

        private final CheckoutSessionRepository checkoutSessionRepository;
        private final ProcessedPaymentEventRepository processedPaymentEventRepository;
        private final CheckoutDownstreamGateway downstreamGateway;
        private final CheckoutMapper checkoutMapper;
        private final CheckoutEventPublisher eventPublisher;

        @Transactional
        public void compensatePaymentFailure(
                        String eventId,
                        String eventType,
                        PaymentResultEventMessage event) {
                log.warn(
                                "event=checkout.payment_failed_received eventId={} paymentId={} checkoutId={} orderId={} reason={}",
                                eventId,
                                event.paymentId(),
                                event.checkoutId(),
                                event.orderId(),
                                event.failureReason());

                if (eventId != null && processedPaymentEventRepository.existsByEventId(eventId)) {
                        log.info(
                                        "event=checkout.payment_event_duplicate eventId={} eventType={}",
                                        eventId,
                                        eventType);
                        return;
                }

                CheckoutSession session = checkoutSessionRepository
                                .findByPaymentIdForUpdate(event.paymentId())
                                .orElseThrow(() -> new BaseException(
                                                CheckoutErrorCode.CHECKOUT_SESSION_NOT_FOUND));

                if (session.getStatus() == CheckoutStatus.COMPENSATED) {
                        markProcessed(eventId, eventType, event, session);
                        return;
                }

                try {
                        log.warn(
                                        "event=checkout.compensation_started checkoutId={} orderId={} paymentId={} inventoryReservationId={} promotionUsageReservationId={}",
                                        session.getId(),
                                        session.getOrderId(),
                                        session.getPaymentId(),
                                        session.getInventoryReservationId(),
                                        session.getPromotionUsageReservationId());

                        session.completeStep(CheckoutSagaStepName.PAYMENT_FAILED_RECEIVED);

                        if (!session.isStepCompleted(CheckoutSagaStepName.INVENTORY_RELEASED)
                                        && session.getInventoryReservationId() != null) {
                                downstreamGateway.releaseReservation(session.getInventoryReservationId());
                                session.completeStep(CheckoutSagaStepName.INVENTORY_RELEASED);
                        }

                        if (!session.isStepCompleted(CheckoutSagaStepName.PROMOTION_CANCELLED)
                                        && session.getOrderId() != null
                                        && session.getPromotionUsageReservationId() != null) {
                                downstreamGateway.cancelPromotionUsage(
                                                session.getPromotionUsageReservationId(),
                                                "PAYMENT_FAILED");
                                session.completeStep(CheckoutSagaStepName.PROMOTION_CANCELLED);
                        }

                        if (!session.isStepCompleted(CheckoutSagaStepName.ORDER_MARKED_PAYMENT_FAILED)
                                        && session.getOrderId() != null) {
                                downstreamGateway.markOrderPaymentFailed(
                                                session.getOrderId(),
                                                checkoutMapper.toMarkOrderPaymentFailedRequest(event));
                                session.completeStep(CheckoutSagaStepName.ORDER_MARKED_PAYMENT_FAILED);
                        }

                        session.markCompensated();
                        session.completeStep(CheckoutSagaStepName.CHECKOUT_COMPENSATED);

                        CheckoutSession saved = checkoutSessionRepository.save(session);

                        markProcessed(eventId, eventType, event, saved);

                        eventPublisher.publishCheckoutCompensated(saved);

                        log.info(
                                        "event=checkout.compensation_completed checkoutId={} orderId={} paymentId={}",
                                        saved.getId(),
                                        saved.getOrderId(),
                                        saved.getPaymentId());
                } catch (Exception ex) {
                        session.markCompensationFailed();
                        session.failStep(
                                        CheckoutSagaStepName.CHECKOUT_COMPENSATED,
                                        ex.getMessage());

                        CheckoutSession saved = checkoutSessionRepository.save(session);

                        eventPublisher.publishCheckoutCompensationFailed(saved);

                        log.error(
                                        "event=checkout.compensation_failed checkoutId={} orderId={} paymentId={}",
                                        saved.getId(),
                                        saved.getOrderId(),
                                        saved.getPaymentId(),
                                        ex);

                        throw ex;
                }
        }

        private void markProcessed(
                        String eventId,
                        String eventType,
                        PaymentResultEventMessage event,
                        CheckoutSession session) {
                if (eventId == null || eventId.isBlank()) {
                        return;
                }

                processedPaymentEventRepository.save(
                                new ProcessedPaymentEvent(
                                                eventId,
                                                eventType,
                                                event.paymentId(),
                                                session.getId()));
        }

        @Transactional
        public CheckoutSessionResponse retryCompensation(UUID checkoutId) {
                CheckoutSession session = checkoutSessionRepository.findByIdForUpdate(checkoutId)
                                .orElseThrow(() -> new BaseException(
                                                CheckoutErrorCode.CHECKOUT_SESSION_NOT_FOUND));

                if (session.getStatus() != CheckoutStatus.COMPENSATION_FAILED) {
                        throw new BaseException(CheckoutErrorCode.CHECKOUT_INVALID_STATUS);
                }

                log.warn(
                                "event=checkout.compensation_retry_requested checkoutId={} orderId={} paymentId={}",
                                session.getId(),
                                session.getOrderId(),
                                session.getPaymentId());

                session.completeStep(CheckoutSagaStepName.COMPENSATION_RETRY_REQUESTED);
                checkoutSessionRepository.save(session);

                PaymentResultEventMessage syntheticEvent = new PaymentResultEventMessage(
                                session.getPaymentId(),
                                session.getId(),
                                session.getOrderId(),
                                session.getUserId(),
                                null,
                                "FAILED",
                                null,
                                session.getGrandTotalAmount(),
                                session.getCurrency(),
                                "Manual compensation retry");

                compensatePaymentFailure(null, "manual.retry.compensation", syntheticEvent);

                CheckoutSession refreshed = checkoutSessionRepository.findById(checkoutId)
                                .orElseThrow(() -> new BaseException(
                                                CheckoutErrorCode.CHECKOUT_SESSION_NOT_FOUND));

                return checkoutMapper.toSessionResponse(refreshed);
        }
}
