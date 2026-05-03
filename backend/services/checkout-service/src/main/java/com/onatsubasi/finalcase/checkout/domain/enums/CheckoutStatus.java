package com.onatsubasi.finalcase.checkout.domain.enums;

public enum CheckoutStatus {
    QUOTE_CREATED,

    STARTED,

    /**
     * Kept temporarily for compatibility with older generated application code.
     * New code should use STARTED.
     */
    SUBMITTED,

    PAYMENT_PENDING,
    PAYMENT_SUCCEEDED,
    PAYMENT_FAILED,

    COMPLETED,
    FAILED,

    COMPENSATION_PENDING,
    COMPENSATED,
    COMPENSATION_FAILED,

    EXPIRED,
    CANCELLED,
    FINALIZATION_FAILED
}