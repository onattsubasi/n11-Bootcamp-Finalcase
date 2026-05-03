package com.onatsubasi.finalcase.order.domain.enums;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    PREPARING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURN_REQUESTED,
    RETURN_APPROVED,
    RETURN_REJECTED,
    REFUNDED;

    public boolean isTerminal() {
        return this == CANCELLED
                || this == DELIVERED
                || this == REFUNDED
                || this == RETURN_REJECTED;
    }
}