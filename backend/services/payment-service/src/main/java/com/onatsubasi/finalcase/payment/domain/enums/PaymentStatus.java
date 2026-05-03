package com.onatsubasi.finalcase.payment.domain.enums;

public enum PaymentStatus {
    INITIATED,
    WAITING_PROVIDER_ACTION,
    AUTHORIZED,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED,
    EXPIRED
}