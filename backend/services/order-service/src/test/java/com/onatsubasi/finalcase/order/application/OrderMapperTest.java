package com.onatsubasi.finalcase.order.application;

import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.domain.entity.Order;
import com.onatsubasi.finalcase.order.infrastructure.mapper.OrderMapper;
import com.onatsubasi.finalcase.order.support.OrderTestData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void mapsCheckoutSnapshotIntoImmutableOrderAggregate() {
        Order order = mapper.toOrder("ORD-20260503-000001", OrderTestData.createOrderRequest());
        order.assertHasItems();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getCheckoutId()).isEqualTo(OrderTestData.CHECKOUT_ID);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getProductName()).isEqualTo("Example Phone");
        assertThat(order.getDiscounts().get(0).getCouponCode()).isEqualTo("WELCOME10");
        assertThat(order.getGrandTotalAmount()).isEqualByComparingTo("190.00");
    }

    @Test
    void mapsOrderToDetailResponseWithoutRecalculatingHistoricalValues() {
        Order order = OrderTestData.paidOrder();

        var response = mapper.toDetailResponse(order);

        assertThat(response.orderNumber()).isEqualTo("ORD-20260503-000001");
        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        assertThat(response.items()).hasSize(1);
        assertThat(response.discounts()).hasSize(1);
        assertThat(response.grandTotalAmount()).isEqualByComparingTo("190.00");
    }
}
