package com.onatsubasi.finalcase.promotion.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum PromotionErrorCode implements ErrorCode {

    PROMOTION_NOT_FOUND("PROMO-001", "Promotion not found", 404),
    PROMOTION_NOT_ACTIVE("PROMO-002", "Promotion is not active", 409),
    PROMOTION_EXPIRED("PROMO-003", "Promotion has expired", 409),
    PROMOTION_USAGE_LIMIT_EXCEEDED("PROMO-004", "Promotion usage limit exceeded", 409),
    PROMOTION_PER_USER_LIMIT_EXCEEDED("PROMO-005", "Promotion per-user usage limit exceeded", 409),

    COUPON_NOT_FOUND("PROMO-006", "Coupon not found", 404),
    COUPON_NOT_ACTIVE("PROMO-007", "Coupon is not active", 409),
    COUPON_EXPIRED("PROMO-008", "Coupon has expired", 409),
    COUPON_USAGE_LIMIT_EXCEEDED("PROMO-009", "Coupon usage limit exceeded", 409),
    COUPON_PER_USER_LIMIT_EXCEEDED("PROMO-010", "Coupon per-user usage limit exceeded", 409),
    COUPON_ASSIGNMENT_NOT_FOUND("PROMO-011", "Coupon assignment not found", 404),
    COUPON_ASSIGNMENT_NOT_ACTIVE("PROMO-012", "Coupon assignment is not active", 409),
    COUPON_ALREADY_EXISTS("PROMO-013", "Coupon already exists", 409),

    PROMOTION_USAGE_RESERVATION_NOT_FOUND("PROMO-020", "Promotion usage reservation not found", 404),
    PROMOTION_USAGE_RESERVATION_NOT_ACTIVE("PROMO-021", "Promotion usage reservation is not active", 409),
    PROMOTION_USAGE_RESERVATION_REDEEM_BLOCKED("PROMO-022", "Cancelled or expired reservation cannot be redeemed", 409),
    PROMOTION_USAGE_RESERVATION_CANCEL_BLOCKED("PROMO-023", "Redeemed reservation cannot be cancelled", 409),

    INVALID_IDEMPOTENCY_KEY("PROMO-030", "Idempotency-Key header is required", 400),
    IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD(
            "PROMO-031",
            "Idempotency key was reused with a different payload",
            409
    ),
    INVALID_REQUEST_HASH("PROMO-032", "Request hash is required", 400),

    INVALID_CHECKOUT_ID("PROMO-040", "Checkout id is required", 400),
    INVALID_ORDER_ID("PROMO-041", "Order id is required", 400),
    INVALID_USER_ID("PROMO-042", "User id is required", 400),

    MINIMUM_BASKET_AMOUNT_NOT_MET("PROMO-050", "Minimum basket amount is not met", 409),
    PROMOTION_QUOTE_FAILED("PROMO-051", "Promotion quote calculation failed", 409),

    INVALID_PROMOTION_DATA("PROMO-060", "Invalid promotion data", 400),
    INVALID_PROMOTION_RULE_CONFIG("PROMO-061", "Invalid promotion rule config", 400),
    INVALID_COUPON_DATA("PROMO-062", "Invalid coupon data", 400),
    INVALID_COUPON_ASSIGNMENT_DATA("PROMO-063", "Invalid coupon assignment data", 400),
    INVALID_PROMOTION_USAGE_RESERVATION("PROMO-064", "Invalid promotion usage reservation", 400),

    PROMOTION_STORAGE_ERROR("PROMO-999", "Promotion storage error", 500);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    PromotionErrorCode(String code, String defaultMessage, int httpStatus) {
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