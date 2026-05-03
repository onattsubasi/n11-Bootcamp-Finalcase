package com.onatsubasi.finalcase.order.domain.enums;

public enum OrderStatusChangeSource {
    CHECKOUT_SERVICE,
    PAYMENT_SERVICE,
    SHIPMENT_SERVICE,
    ADMIN,
    CUSTOMER,
    SYSTEM
}