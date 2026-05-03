package com.onatsubasi.finalcase.order.infrastructure.messaging;

public final class OrderEventTypes {

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_PAID = "order.paid";
    public static final String ORDER_PAYMENT_FAILED = "order.payment_failed";
    public static final String ORDER_CANCELLED = "order.cancelled";
    public static final String ORDER_PREPARING = "order.preparing";
    public static final String ORDER_SHIPPED = "order.shipped";
    public static final String ORDER_DELIVERED = "order.delivered";

    private OrderEventTypes() {
    }
}