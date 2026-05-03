package com.onatsubasi.finalcase.basket.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum BasketErrorCode implements ErrorCode {

    UNAUTHENTICATED_BASKET_ACCESS("BASKET-001", "Authentication is required to access basket", 401),
    INVALID_PRODUCT_ID("BASKET-002", "Product id is required", 400),
    INVALID_QUANTITY("BASKET-003", "Quantity must be greater than zero", 400),
    BASKET_ITEM_NOT_FOUND("BASKET-004", "Basket item not found", 404),
    BASKET_ITEM_QUANTITY_LIMIT_EXCEEDED("BASKET-005", "Basket item quantity limit exceeded", 400),
    BASKET_NOT_FOUND("BASKET-006", "Active basket not found", 404),
    BASKET_EMPTY("BASKET-007", "Basket is empty", 409),
    BASKET_ALREADY_CHECKED_OUT("BASKET-008", "Basket already checked out", 409),
    BASKET_NOT_ACTIVE("BASKET-009", "Basket is not active", 409),
    BASKET_INVALID_QUANTITY("BASKET-010", "Invalid item quantity in basket", 400),
    INVALID_ORDER_ID("BASKET-011", "Order id is required", 400),
    INVALID_COUPON_CODE_INTENT("BASKET-012", "Invalid coupon code intent", 400),
    BASKET_OWNERSHIP_VIOLATION("BASKET-013", "Basket does not belong to current user", 403),

    BASKET_STORAGE_ERROR("BASKET-999", "Basket storage error", 500);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    BasketErrorCode(String code, String defaultMessage, int httpStatus) {
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