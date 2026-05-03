package com.onatsubasi.finalcase.order.domain;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatusChangeSource;
import com.onatsubasi.finalcase.order.domain.entity.Order;
import com.onatsubasi.finalcase.order.domain.entity.OrderItem;
import com.onatsubasi.finalcase.order.support.OrderTestData;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderDomainTest {

    @Test
    void createdOrderStartsPendingAndStoresSnapshots() {
        Order order = OrderTestData.pendingOrder();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getDiscounts()).hasSize(1);
        assertThat(order.getShippingAddress().getCity()).isEqualTo("İstanbul");
        assertThat(order.getStatusHistory()).hasSize(1);
    }

    @Test
    void markPaidIsIdempotentButOppositePaymentFinalizationConflicts() {
        Order order = OrderTestData.pendingOrder();

        order.markPaid(OrderTestData.PAYMENT_ID, "IYZICO", "SUCCEEDED", "tx-1", OrderStatusChangeSource.PAYMENT_SERVICE);
        order.markPaid(OrderTestData.PAYMENT_ID, "IYZICO", "SUCCEEDED", "tx-1", OrderStatusChangeSource.PAYMENT_SERVICE);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentSummary().getPaymentProvider()).isEqualTo("IYZICO");
        assertThat(order.getStatusHistory()).extracting("toStatus").containsExactly(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);

        assertThatThrownBy(() -> order.markPaymentFailed(OrderTestData.PAYMENT_ID, "IYZICO", "FAILED", "tx-1", OrderStatusChangeSource.PAYMENT_SERVICE))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void shipmentCanOnlyBeAttachedAfterPaymentSuccess() {
        Order pending = OrderTestData.pendingOrder();

        assertThatThrownBy(() -> pending.attachShipmentCreated(OrderTestData.SHIPMENT_ID, "SHP-1", "MANUAL", "TRK-1", "CREATED"))
                .isInstanceOf(BaseException.class);

        Order paid = OrderTestData.paidOrder();
        paid.attachShipmentCreated(OrderTestData.SHIPMENT_ID, "SHP-1", "MANUAL", "TRK-1", "CREATED");

        assertThat(paid.getShipmentSummary().getShipmentId()).isEqualTo(OrderTestData.SHIPMENT_ID);
    }

    @Test
    void shippedOrderCanBeDelivered() {
        Order order = OrderTestData.paidOrder();

        order.markShipped("MANUAL", "TRK-1", Instant.parse("2026-05-03T10:00:00Z"), OrderStatusChangeSource.SHIPMENT_SERVICE);
        order.markDelivered(Instant.parse("2026-05-04T10:00:00Z"), OrderStatusChangeSource.SHIPMENT_SERVICE);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getShipmentSummary().getShipmentStatus()).isEqualTo("DELIVERED");
        assertThat(order.getShipmentSummary().getDeliveredAt()).isEqualTo(Instant.parse("2026-05-04T10:00:00Z"));
    }

    @Test
    void itemRejectsInconsistentLineTotals() {
        assertThatThrownBy(() -> new OrderItem(
                "product-1",
                "SKU-1",
                "Example Phone",
                "example-phone",
                null,
                null,
                null,
                null,
                null,
                OrderTestData.money("100.00"),
                2,
                OrderTestData.money("199.00"),
                OrderTestData.money("0.00"),
                OrderTestData.money("199.00"),
                "TRY"
        )).isInstanceOf(BaseException.class);
    }
}
