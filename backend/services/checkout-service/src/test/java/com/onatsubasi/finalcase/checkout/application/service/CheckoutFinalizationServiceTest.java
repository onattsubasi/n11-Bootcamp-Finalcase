package com.onatsubasi.finalcase.checkout.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.application.dto.event.PaymentResultEventMessage;
import com.onatsubasi.finalcase.checkout.application.port.CheckoutEventPublisher;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.domain.entity.ProcessedPaymentEvent;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepName;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;
import com.onatsubasi.finalcase.checkout.domain.repository.CheckoutSessionRepository;
import com.onatsubasi.finalcase.checkout.domain.repository.ProcessedPaymentEventRepository;
import com.onatsubasi.finalcase.checkout.infrastructure.mapper.CheckoutMapper;
import com.onatsubasi.finalcase.checkout.support.CheckoutTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutFinalizationServiceTest {

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private ProcessedPaymentEventRepository processedPaymentEventRepository;

    @Mock
    private CheckoutDownstreamGateway downstreamGateway;

    @Spy
    private CheckoutMapper checkoutMapper = new CheckoutMapper(new ObjectMapper());

    @Mock
    private CheckoutEventPublisher eventPublisher;

    @InjectMocks
    private CheckoutFinalizationService finalizationService;

    @Test
    void finalizePaymentSuccess_ignoresDuplicateEventBeforeLoadingCheckout() {
        String eventId = UUID.randomUUID().toString();
        UUID paymentId = UUID.randomUUID();
        when(processedPaymentEventRepository.existsByEventId(eventId)).thenReturn(true);

        finalizationService.finalizePaymentSuccess(
                eventId,
                "payment.succeeded",
                CheckoutTestFixtures.paymentSucceededEvent(paymentId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        );

        verify(checkoutSessionRepository, never()).findByPaymentIdForUpdate(any());
        verify(downstreamGateway, never()).confirmReservation(any(), any());
    }

    @Test
    void finalizePaymentSuccess_completesAllMissingSagaStepsAndPublishesCompletion() {
        String eventId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        CheckoutSession session = CheckoutTestFixtures.paymentPendingSession(userId, basketId, paymentId, orderId);
        PaymentResultEventMessage event = CheckoutTestFixtures.paymentSucceededEvent(paymentId, UUID.randomUUID(), orderId, userId);

        when(processedPaymentEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(checkoutSessionRepository.findByPaymentIdForUpdate(paymentId)).thenReturn(Optional.of(session));
        when(downstreamGateway.createShipmentForOrder(orderId)).thenReturn(CheckoutTestFixtures.shipment(shipmentId));
        when(checkoutSessionRepository.save(any(CheckoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processedPaymentEventRepository.save(any(ProcessedPaymentEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        finalizationService.finalizePaymentSuccess(eventId, "payment.succeeded", event);

        verify(downstreamGateway).confirmReservation(session.getInventoryReservationId(), orderId);
        verify(downstreamGateway).redeemPromotionUsage(session.getPromotionUsageReservationId(), orderId);
        verify(downstreamGateway).markOrderPaid(any(), any());
        verify(downstreamGateway).markBasketCheckedOut(basketId, session.getId(), orderId);
        verify(downstreamGateway).createShipmentForOrder(orderId);
        verify(eventPublisher).publishCheckoutCompleted(session);

        assertThat(session.getStatus()).isEqualTo(CheckoutStatus.COMPLETED);
        assertThat(session.getShipmentId()).isEqualTo(shipmentId);
        assertThat(session.isStepCompleted(CheckoutSagaStepName.INVENTORY_CONFIRMED)).isTrue();
        assertThat(session.isStepCompleted(CheckoutSagaStepName.PROMOTION_REDEEMED)).isTrue();
        assertThat(session.isStepCompleted(CheckoutSagaStepName.ORDER_MARKED_PAID)).isTrue();
        assertThat(session.isStepCompleted(CheckoutSagaStepName.BASKET_MARKED_CHECKED_OUT)).isTrue();
        assertThat(session.isStepCompleted(CheckoutSagaStepName.SHIPMENT_CREATED)).isTrue();

        ArgumentCaptor<ProcessedPaymentEvent> processedEventCaptor = ArgumentCaptor.forClass(ProcessedPaymentEvent.class);
        verify(processedPaymentEventRepository).save(processedEventCaptor.capture());
        assertThat(processedEventCaptor.getValue().getEventId()).isEqualTo(eventId);
        assertThat(processedEventCaptor.getValue().getPaymentId()).isEqualTo(paymentId);
    }
}
