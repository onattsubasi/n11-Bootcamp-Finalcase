package com.onatsubasi.finalcase.checkout.infrastructure.messaging;

public final class CheckoutEventTypes {

    public static final String CHECKOUT_SUBMITTED = "checkout.submitted";
    public static final String CHECKOUT_PAYMENT_PENDING = "checkout.payment_pending";
    public static final String CHECKOUT_COMPLETED = "checkout.completed";
    public static final String CHECKOUT_FAILED = "checkout.failed";
    public static final String CHECKOUT_COMPENSATED = "checkout.compensated";
    public static final String CHECKOUT_FINALIZATION_FAILED = "checkout.finalization_failed";
    public static final String CHECKOUT_COMPENSATION_FAILED = "checkout.compensation_failed";
    public static final String CHECKOUT_CANCELLED = "checkout.cancelled";

    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String PAYMENT_FAILED = "payment.failed";

    private CheckoutEventTypes() {
    }
}