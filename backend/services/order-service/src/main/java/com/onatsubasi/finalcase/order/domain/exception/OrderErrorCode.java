package com.onatsubasi.finalcase.order.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND("ORDER-001", "Order not found", 404),
    ORDER_ACCESS_DENIED("ORDER-002", "Order access denied", 403),
    ORDER_INVALID_STATUS_TRANSITION("ORDER-003", "Invalid order status transition", 409),
    ORDER_ALREADY_EXISTS_FOR_CHECKOUT("ORDER-004", "Order already exists for checkout", 409),
    ORDER_IDEMPOTENCY_CONFLICT("ORDER-005", "Order idempotency key reused with different payload", 409),
    INVALID_ORDER_DATA("ORDER-006", "Invalid order data", 400),
    INVALID_ORDER_ITEM_DATA("ORDER-007", "Invalid order item data", 400),
    INVALID_ORDER_TOTALS("ORDER-008", "Invalid order totals", 400),
    INVALID_ORDER_ADDRESS("ORDER-009", "Invalid order address snapshot", 400),
    INVALID_ORDER_DISCOUNT("ORDER-010", "Invalid order discount snapshot", 400),
    ORDER_CANNOT_BE_CANCELLED("ORDER-011", "Order cannot be cancelled in current status", 409),
    ORDER_PAYMENT_ALREADY_FINALIZED("ORDER-012", "Order payment is already finalized", 409),
    ORDER_SHIPMENT_ALREADY_ATTACHED("ORDER-013", "Order shipment is already attached", 409),
    ORDER_NUMBER_GENERATION_FAILED("ORDER-014", "Order number generation failed", 409),
    REVIEW_ORDER_ITEM_NOT_ELIGIBLE("ORDER-015", "Order item is not eligible for review", 409);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    OrderErrorCode(String code, String defaultMessage, int httpStatus) {
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
