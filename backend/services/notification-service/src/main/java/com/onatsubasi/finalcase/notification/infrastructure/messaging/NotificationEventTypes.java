package com.onatsubasi.finalcase.notification.infrastructure.messaging;

public final class NotificationEventTypes {

    public static final String NOTIFICATION_CREATED = "notification.created";
    public static final String NOTIFICATION_SENT = "notification.sent";
    public static final String NOTIFICATION_FAILED = "notification.failed";
    public static final String NOTIFICATION_DELIVERY_RETRY_SCHEDULED =
            "notification.delivery_retry_scheduled";

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_PAID = "order.paid";
    public static final String ORDER_PAYMENT_FAILED = "order.payment_failed";
    public static final String ORDER_CANCELLED = "order.cancelled";

    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_REFUNDED = "payment.refunded";

    public static final String SHIPMENT_CREATED = "shipment.created";
    public static final String SHIPMENT_SHIPPED = "shipment.shipped";
    public static final String SHIPMENT_DELIVERED = "shipment.delivered";
    public static final String SHIPMENT_DELIVERY_FAILED = "shipment.delivery_failed";
    public static final String SHIPMENT_CANCELLED = "shipment.cancelled";

    public static final String CHECKOUT_COMPLETED = "checkout.completed";
    public static final String CHECKOUT_FAILED = "checkout.failed";
    public static final String CHECKOUT_FINALIZATION_FAILED =
            "checkout.finalization_failed";

    public static final String INVENTORY_LOW_STOCK = "inventory.low_stock";
    public static final String INVENTORY_OUT_OF_STOCK = "inventory.out_of_stock";
    public static final String INVENTORY_BACK_IN_STOCK = "inventory.back_in_stock";

    public static final String PROMOTION_AVAILABLE = "promotion.available";
    public static final String PROMOTION_EXPIRING_SOON = "promotion.expiring_soon";
    public static final String COUPON_ASSIGNED = "coupon.assigned";

    private NotificationEventTypes() {
    }
}