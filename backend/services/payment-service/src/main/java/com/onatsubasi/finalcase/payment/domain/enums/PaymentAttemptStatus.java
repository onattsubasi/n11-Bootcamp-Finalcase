package com.onatsubasi.finalcase.payment.domain.enums;

public enum PaymentAttemptStatus {
    CREATED,
    INITIALIZED,
    WAITING_PROVIDER_ACTION,
    PROVIDER_PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    EXPIRED
}