package com.onatsubasi.finalcase.checkout.domain.entity;

import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepName;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckoutSessionTest {

    @Test
    void startAndCompletePaymentLifecycle_recordsCriticalSagaSteps() {
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        UUID inventoryReservationId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();

        CheckoutSession session = CheckoutSession.start(
                userId,
                "idem-1",
                "hash-1",
                "TRY",
                Instant.now().plusSeconds(1800)
        );

        session.attachBasket(basketId);
        session.replaceTotals(
                money("2000.00"),
                money("0.00"),
                money("100.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("1900.00")
        );
        session.attachInventoryReservation(inventoryReservationId);
        session.attachOrder(orderId, "ORD-20260503-000001");
        session.attachPaymentAction(paymentId, UUID.randomUUID(), "provider-session", "https://payment.example", Map.of("provider", "IYZICO"));
        session.markPaymentSucceeded();
        session.markCompleted(shipmentId);

        assertThat(session.getStatus()).isEqualTo(CheckoutStatus.COMPLETED);
        assertThat(session.getBasketId()).isEqualTo(basketId);
        assertThat(session.getOrderId()).isEqualTo(orderId);
        assertThat(session.getPaymentId()).isEqualTo(paymentId);
        assertThat(session.getShipmentId()).isEqualTo(shipmentId);
        assertThat(session.isStepCompleted(CheckoutSagaStepName.INVENTORY_RESERVED)).isTrue();
        assertThat(session.isStepCompleted(CheckoutSagaStepName.ORDER_CREATED)).isTrue();
        assertThat(session.isStepCompleted(CheckoutSagaStepName.PAYMENT_INITIALIZED)).isTrue();
        assertThat(session.isStepCompleted(CheckoutSagaStepName.CHECKOUT_COMPLETED)).isTrue();
    }

    @Test
    void replaceTotals_rejectsArithmeticMismatch() {
        CheckoutSession session = CheckoutSession.start(
                UUID.randomUUID(),
                "idem-2",
                "hash-2",
                "TRY",
                null
        );

        assertThatThrownBy(() -> session.replaceTotals(
                money("100.00"),
                money("0.00"),
                money("10.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("100.00")
        ))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("Grand total");
    }

    @Test
    void terminalCheckoutRejectsLateInventoryAttachment() {
        CheckoutSession session = CheckoutSession.start(
                UUID.randomUUID(),
                "idem-3",
                "hash-3",
                "TRY",
                null
        );

        session.cancel("customer cancelled");

        assertThatThrownBy(() -> session.attachInventoryReservation(UUID.randomUUID()))
                .isInstanceOf(BaseException.class);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
