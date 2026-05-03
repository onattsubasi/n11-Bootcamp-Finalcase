package com.onatsubasi.finalcase.user.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum UserErrorCode implements ErrorCode {

    USER_PROFILE_NOT_FOUND("USER-001", "User profile not found", 404),
    USER_PROFILE_ALREADY_EXISTS("USER-002", "User profile already exists", 409),
    USER_PROFILE_DISABLED("USER-003", "User profile is disabled", 409),
    USER_PROFILE_DELETED("USER-004", "User profile is deleted", 409),

    ADDRESS_NOT_FOUND("USER-010", "Address not found", 404),
    ADDRESS_ACCESS_DENIED("USER-011", "Address not found", 404),
    DEFAULT_SHIPPING_ADDRESS_NOT_FOUND("USER-012", "Default shipping address not found", 404),
    DEFAULT_BILLING_ADDRESS_NOT_FOUND("USER-013", "Default billing address not found", 404),

    FAVORITE_NOT_FOUND("USER-020", "Favorite product not found", 404),
    FAVORITE_ALREADY_EXISTS("USER-021", "Favorite product already exists", 409),

    PRODUCT_LIST_NOT_FOUND("USER-030", "Product list not found", 404),
    PRODUCT_LIST_ITEM_NOT_FOUND("USER-031", "Product list item not found", 404),
    PRODUCT_LIST_ITEM_ALREADY_EXISTS("USER-032", "Product already exists in product list", 409),

    INVALID_USER_ID("USER-040", "User id is required", 400),
    INVALID_EMAIL("USER-041", "Invalid email", 400),
    INVALID_PROFILE_DATA("USER-042", "Invalid user profile data", 400),
    INVALID_ADDRESS_DATA("USER-043", "Invalid address data", 400),
    INVALID_PRODUCT_ID("USER-044", "Product id is required", 400),
    INVALID_PRODUCT_LIST_DATA("USER-045", "Invalid product list data", 400),
    INVALID_PREFERENCE_DATA("USER-046", "Invalid user preference data", 400),

    USER_STORAGE_ERROR("USER-999", "User storage error", 500);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    UserErrorCode(String code, String defaultMessage, int httpStatus) {
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