package com.onatsubasi.finalcase.order.application;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.application.service.OrderQueryService;
import com.onatsubasi.finalcase.order.domain.repository.OrderRepository;
import com.onatsubasi.finalcase.order.infrastructure.mapper.OrderMapper;
import com.onatsubasi.finalcase.order.support.OrderTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderQueryServiceTest {

    private OrderRepository orderRepository;
    private OrderQueryService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        service = new OrderQueryService(orderRepository, new OrderMapper());
    }

    @Test
    void customerCannotReadAnotherUsersOrder() {
        when(orderRepository.findById(OrderTestData.ORDER_ID)).thenReturn(Optional.of(OrderTestData.pendingOrder()));

        assertThatThrownBy(() -> service.getByIdForCustomer(OrderTestData.ORDER_ID, OrderTestData.OTHER_USER_ID))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void reviewEligibilityRequiresDeliveredOrderAndMatchingProduct() {
        when(orderRepository.findById(OrderTestData.ORDER_ID)).thenReturn(Optional.of(OrderTestData.pendingOrder()));

        var pendingResult = service.verifyReviewEligibility(OrderTestData.ORDER_ID, OrderTestData.ORDER_ITEM_ID, OrderTestData.USER_ID, "product-1");

        assertThat(pendingResult.eligible()).isFalse();
        assertThat(pendingResult.reason()).isEqualTo("ORDER_NOT_DELIVERED");
    }

    @Test
    void deliveredOrderWithMatchingItemIsReviewEligible() {
        when(orderRepository.findById(OrderTestData.ORDER_ID)).thenReturn(Optional.of(OrderTestData.deliveredOrder()));

        var result = service.verifyReviewEligibility(OrderTestData.ORDER_ID, OrderTestData.ORDER_ITEM_ID, OrderTestData.USER_ID, "product-1");

        assertThat(result.eligible()).isTrue();
        assertThat(result.reason()).isNull();
    }
}
