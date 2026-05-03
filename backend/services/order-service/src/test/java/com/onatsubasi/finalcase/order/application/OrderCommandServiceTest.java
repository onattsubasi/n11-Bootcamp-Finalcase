package com.onatsubasi.finalcase.order.application;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.application.port.OrderEventPublisher;
import com.onatsubasi.finalcase.order.application.service.OrderCommandService;
import com.onatsubasi.finalcase.order.application.service.OrderNumberGenerator;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.domain.entity.Order;
import com.onatsubasi.finalcase.order.domain.repository.OrderRepository;
import com.onatsubasi.finalcase.order.infrastructure.mapper.OrderMapper;
import com.onatsubasi.finalcase.order.support.OrderTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderCommandServiceTest {

    private OrderRepository orderRepository;
    private OrderEventPublisher eventPublisher;
    private OrderCommandService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(OrderEventPublisher.class);
        OrderMapper mapper = new OrderMapper();
        OrderNumberGenerator generator = mock(OrderNumberGenerator.class);
        when(generator.generate()).thenReturn("ORD-20260503-000001");
        service = new OrderCommandService(orderRepository, mapper, generator, eventPublisher);
    }

    @Test
    void createOrderCreatesNewOrderAndPublishesCreatedEvent() {
        when(orderRepository.findByCheckoutIdForUpdate(OrderTestData.CHECKOUT_ID)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createOrder(OrderTestData.createOrderRequest());

        assertThat(response.orderNumber()).isEqualTo("ORD-20260503-000001");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishOrderCreated(any(Order.class));
    }

    @Test
    void createOrderReturnsExistingOrderForSameCheckoutAndSameRequestHash() {
        Order existing = OrderTestData.pendingOrder();
        when(orderRepository.findByCheckoutIdForUpdate(OrderTestData.CHECKOUT_ID)).thenReturn(Optional.of(existing));

        var response = service.createOrder(OrderTestData.createOrderRequest("hash-1"));

        assertThat(response.id()).isEqualTo(OrderTestData.ORDER_ID);
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createOrderRejectsSameCheckoutWithDifferentRequestHash() {
        Order existing = OrderTestData.pendingOrder();
        when(orderRepository.findByCheckoutIdForUpdate(OrderTestData.CHECKOUT_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createOrder(OrderTestData.createOrderRequest("different-hash")))
                .isInstanceOf(BaseException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void markPaidUpdatesPaymentSummaryAndPublishesPaidEvent() {
        Order order = OrderTestData.pendingOrder();
        when(orderRepository.findByIdForUpdate(OrderTestData.ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var response = service.markPaid(OrderTestData.ORDER_ID, OrderTestData.markPaidRequest());

        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        verify(eventPublisher).publishOrderPaid(order);
    }

    @Test
    void customerCancelValidatesOrderOwnership() {
        Order order = OrderTestData.pendingOrder();
        when(orderRepository.findByIdForUpdate(OrderTestData.ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelByCustomer(OrderTestData.ORDER_ID, OrderTestData.OTHER_USER_ID, null))
                .isInstanceOf(BaseException.class);

        verify(orderRepository, never()).save(any());
    }
}
