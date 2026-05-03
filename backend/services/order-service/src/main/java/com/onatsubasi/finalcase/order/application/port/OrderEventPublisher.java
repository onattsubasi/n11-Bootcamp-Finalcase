package com.onatsubasi.finalcase.order.application.port;

import com.onatsubasi.finalcase.order.domain.entity.Order;

public interface OrderEventPublisher {

    void publishOrderCreated(Order order);

    void publishOrderPaid(Order order);

    void publishOrderPaymentFailed(Order order);

    void publishOrderCancelled(Order order);

    void publishOrderPreparing(Order order);

    void publishOrderShipped(Order order);

    void publishOrderDelivered(Order order);
}