package com.onatsubasi.finalcase.payment.infrastructure.messaging;
public final class PaymentEventTypes {

    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_CANCELLED = "payment.cancelled";
    public static final String PAYMENT_REFUNDED = "payment.refunded";

    private PaymentEventTypes() {
    }
}