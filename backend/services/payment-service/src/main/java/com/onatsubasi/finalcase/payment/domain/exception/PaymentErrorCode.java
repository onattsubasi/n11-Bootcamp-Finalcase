package com.onatsubasi.finalcase.payment.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_NOT_FOUND("PAYMENT-001", "Payment not found", 404),
    PAYMENT_ATTEMPT_NOT_FOUND("PAYMENT-002", "Payment attempt not found", 404),
    PAYMENT_ALREADY_SUCCEEDED("PAYMENT-003", "Payment already succeeded", 409),
    PAYMENT_ALREADY_FINALIZED("PAYMENT-004", "Payment is already finalized", 409),
    PAYMENT_INVALID_STATUS("PAYMENT-005", "Payment status does not allow this operation", 409),

    PAYMENT_IDEMPOTENCY_KEY_REQUIRED("PAYMENT-006", "Idempotency-Key header is required", 400),
    PAYMENT_IDEMPOTENCY_CONFLICT("PAYMENT-007", "Idempotency key reused with different request payload", 409),

    PAYMENT_PROVIDER_NOT_SUPPORTED("PAYMENT-008", "Payment provider is not supported", 400),
    PAYMENT_METHOD_NOT_SUPPORTED("PAYMENT-009", "Payment method is not supported", 400),
    PAYMENT_PROVIDER_METHOD_NOT_SUPPORTED("PAYMENT-010", "Payment provider does not support requested method", 400),

    PAYMENT_INITIALIZE_FAILED("PAYMENT-011", "Payment initialization failed", 502),
    PAYMENT_PROVIDER_RETRIEVE_FAILED("PAYMENT-012", "Payment provider retrieve failed", 502),
    PAYMENT_CALLBACK_INVALID("PAYMENT-013", "Payment callback is invalid", 400),
    PAYMENT_CALLBACK_ALREADY_PROCESSED("PAYMENT-014", "Payment callback already processed", 409),

    PAYMENT_AMOUNT_INVALID("PAYMENT-015", "Payment amount is invalid", 400),
    PAYMENT_CURRENCY_INVALID("PAYMENT-016", "Payment currency is invalid", 400),
    PAYMENT_REFUND_AMOUNT_INVALID("PAYMENT-017", "Refund amount is invalid", 400),
    PAYMENT_REFUND_NOT_ALLOWED("PAYMENT-018", "Refund is not allowed for this payment", 409),
    PAYMENT_CANCEL_NOT_ALLOWED("PAYMENT-019", "Cancel is not allowed for this payment", 409),

    PAYMENT_PROVIDER_CONFIGURATION_INVALID("PAYMENT-020", "Payment provider configuration is invalid", 500),
    PAYMENT_PROVIDER_UNAVAILABLE("PAYMENT-021", "Payment provider is temporarily unavailable", 503),
    INVALID_PAYMENT_DATA("PAYMENT-022", "Invalid payment data", 400);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    PaymentErrorCode(String code, String defaultMessage, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }
}
