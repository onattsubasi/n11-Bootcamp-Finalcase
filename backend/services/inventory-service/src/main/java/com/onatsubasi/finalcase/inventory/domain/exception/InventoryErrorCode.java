package com.onatsubasi.finalcase.inventory.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum InventoryErrorCode implements ErrorCode {

    INVENTORY_ITEM_NOT_FOUND("INVENTORY-001", "Inventory item not found", 404),
    INVENTORY_ITEM_ALREADY_EXISTS("INVENTORY-002", "Inventory item already exists for product", 409),

    INVALID_PRODUCT_ID("INVENTORY-010", "Product id is required", 400),
    INVALID_QUANTITY("INVENTORY-011", "Quantity must be greater than or equal to zero", 400),
    INVALID_POSITIVE_QUANTITY("INVENTORY-012", "Quantity must be greater than zero", 400),
    INVALID_LOW_STOCK_THRESHOLD("INVENTORY-013", "Low stock threshold cannot be negative", 400),

    INSUFFICIENT_STOCK("INVENTORY-020", "Insufficient available stock", 409),
    TOTAL_QUANTITY_BELOW_RESERVED("INVENTORY-021", "Total quantity cannot be lower than reserved quantity", 409),
    INVENTORY_ITEM_NOT_ACTIVE("INVENTORY-022", "Inventory item is not active", 409),

    RESERVATION_NOT_FOUND("INVENTORY-030", "Stock reservation not found", 404),
    RESERVATION_EMPTY("INVENTORY-031", "Reservation must contain at least one item", 400),
    RESERVATION_ALREADY_CONFIRMED("INVENTORY-032", "Reservation is already confirmed", 409),
    RESERVATION_ALREADY_RELEASED("INVENTORY-033", "Reservation is already released", 409),
    RESERVATION_ALREADY_EXPIRED("INVENTORY-034", "Reservation is already expired", 409),
    RESERVATION_CONFIRM_BLOCKED("INVENTORY-035", "Released or expired reservation cannot be confirmed", 409),
    RESERVATION_RELEASE_BLOCKED("INVENTORY-036", "Confirmed reservation cannot be released", 409),
    RESERVATION_EXPIRED("INVENTORY-037", "Reservation is expired", 409),
    INVALID_RESERVATION_STATUS("INVENTORY-038", "Invalid reservation status transition", 409),

    INVALID_IDEMPOTENCY_KEY("INVENTORY-040", "Idempotency-Key header is required", 400),
    IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD(
            "INVENTORY-041",
            "Idempotency key was reused with a different payload",
            409
    ),
    INVALID_REQUEST_HASH("INVENTORY-042", "Request hash is required", 400),

    INVALID_CHECKOUT_ID("INVENTORY-050", "Checkout id is required", 400),
    INVALID_ORDER_ID("INVENTORY-051", "Order id is required", 400),
    INVALID_USER_ID("INVENTORY-052", "User id is required", 400),

    INVENTORY_STORAGE_ERROR("INVENTORY-999", "Inventory storage error", 500);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    InventoryErrorCode(String code, String defaultMessage, int httpStatus) {
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