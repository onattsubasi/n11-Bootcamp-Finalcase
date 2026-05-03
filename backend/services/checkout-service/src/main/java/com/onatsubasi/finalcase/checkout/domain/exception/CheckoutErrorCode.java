package com.onatsubasi.finalcase.checkout.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum CheckoutErrorCode implements ErrorCode {

    CHECKOUT_SESSION_NOT_FOUND("CHECKOUT-001", "Checkout session not found", 404),
    CHECKOUT_ACCESS_DENIED("CHECKOUT-002", "Checkout access denied", 403),

    CHECKOUT_IDEMPOTENCY_KEY_REQUIRED("CHECKOUT-003", "Idempotency-Key header is required", 400),
    CHECKOUT_IDEMPOTENCY_CONFLICT("CHECKOUT-004", "Idempotency key reused with different request payload", 409),
    CHECKOUT_IDEMPOTENCY_RECORD_NOT_FOUND("CHECKOUT-005", "Checkout idempotency record not found", 404),
    CHECKOUT_IDEMPOTENCY_IN_PROGRESS("CHECKOUT-006", "Checkout request is already in progress", 409),

    CHECKOUT_ALREADY_COMPLETED("CHECKOUT-007", "Checkout is already completed", 409),
    CHECKOUT_INVALID_STATUS("CHECKOUT-008", "Checkout status does not allow this operation", 409),

    CHECKOUT_QUOTE_FAILED("CHECKOUT-009", "Checkout quote calculation failed", 409),
    CHECKOUT_SUBMIT_FAILED("CHECKOUT-010", "Checkout submit failed", 409),
    CHECKOUT_FINALIZATION_FAILED("CHECKOUT-011", "Checkout finalization failed", 409),
    CHECKOUT_COMPENSATION_FAILED("CHECKOUT-012", "Checkout compensation failed", 409),

    CHECKOUT_BASKET_EMPTY("CHECKOUT-013", "Basket is empty", 409),
    CHECKOUT_PRODUCT_NOT_SELLABLE("CHECKOUT-014", "Product is not sellable", 409),
    CHECKOUT_INSUFFICIENT_STOCK("CHECKOUT-015", "Insufficient stock", 409),

    INVALID_CHECKOUT_DATA("CHECKOUT-016", "Invalid checkout data", 400),
    INVALID_CHECKOUT_TOTALS("CHECKOUT-017", "Invalid checkout totals", 400),
    INVALID_PAYMENT_EVENT("CHECKOUT-018", "Invalid payment event", 400),
    PAYMENT_EVENT_ALREADY_PROCESSED("CHECKOUT-019", "Payment event already processed", 409),

    CHECKOUT_ITEM_NOT_FOUND("CHECKOUT-020", "Checkout item not found", 404),
    CHECKOUT_ADDRESS_NOT_FOUND("CHECKOUT-021", "Checkout address not found", 404),
    CHECKOUT_DISCOUNT_NOT_FOUND("CHECKOUT-022", "Checkout discount not found", 404),
    CHECKOUT_SAGA_STEP_NOT_FOUND("CHECKOUT-023", "Checkout saga step not found", 404),

    DOWNSTREAM_BASKET_FAILED("CHECKOUT-030", "Basket service call failed", 502),
    DOWNSTREAM_CATALOG_FAILED("CHECKOUT-031", "Catalog service call failed", 502),
    DOWNSTREAM_USER_FAILED("CHECKOUT-032", "User service call failed", 502),
    DOWNSTREAM_INVENTORY_FAILED("CHECKOUT-033", "Inventory service call failed", 502),
    DOWNSTREAM_PROMOTION_FAILED("CHECKOUT-034", "Promotion service call failed", 502),
    DOWNSTREAM_ORDER_FAILED("CHECKOUT-035", "Order service call failed", 502),
    DOWNSTREAM_PAYMENT_FAILED("CHECKOUT-036", "Payment service call failed", 502),
    DOWNSTREAM_SHIPMENT_FAILED("CHECKOUT-037", "Shipment service call failed", 502),

    PROMOTION_COUPON_VALIDATION_UNAVAILABLE(
            "CHECKOUT-038",
            "Coupon validation is temporarily unavailable. Please try again.",
            503
    ),

    DOWNSTREAM_SERVICE_UNAVAILABLE(
            "CHECKOUT-039",
            "A required checkout dependency is temporarily unavailable",
            503
    ),

    CHECKOUT_STORAGE_ERROR("CHECKOUT-999", "Checkout storage error", 500);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    CheckoutErrorCode(String code, String defaultMessage, int httpStatus) {
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