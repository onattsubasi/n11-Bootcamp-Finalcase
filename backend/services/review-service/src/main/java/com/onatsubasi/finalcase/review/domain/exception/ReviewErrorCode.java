package com.onatsubasi.finalcase.review.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum ReviewErrorCode implements ErrorCode {

    REVIEW_NOT_FOUND("REVIEW-001", "Review not found", 404),
    REVIEW_ACCESS_DENIED("REVIEW-002", "Review access denied", 403),
    REVIEW_ALREADY_EXISTS("REVIEW-003", "Customer already has an active review for this product", 409),

    REVIEW_PURCHASE_NOT_VERIFIED("REVIEW-004", "Customer has not purchased and received this product", 409),
    REVIEW_INVALID_RATING("REVIEW-005", "Review rating must be between 1 and 5", 400),
    REVIEW_INVALID_STATUS("REVIEW-006", "Review status does not allow this operation", 409),
    REVIEW_INVALID_DATA("REVIEW-007", "Invalid review data", 400),

    REVIEW_VOTE_NOT_FOUND("REVIEW-008", "Review vote not found", 404),
    REVIEW_SELF_VOTE_NOT_ALLOWED("REVIEW-009", "Customer cannot vote on their own review", 409),

    REVIEW_REPORT_NOT_FOUND("REVIEW-010", "Review report not found", 404),
    REVIEW_SELF_REPORT_NOT_ALLOWED("REVIEW-011", "Customer cannot report their own review", 409),
    REVIEW_ALREADY_REPORTED("REVIEW-012", "Customer already reported this review", 409),

    RATING_SUMMARY_NOT_FOUND("REVIEW-013", "Product rating summary not found", 404),

    ORDER_SERVICE_UNAVAILABLE("REVIEW-014", "Order service is temporarily unavailable", 503),
    PURCHASE_VERIFICATION_FAILED("REVIEW-015", "Purchase verification failed", 502),

    REVIEW_EVENT_PUBLISH_FAILED("REVIEW-016", "Review event publish failed", 502),

    INVALID_PRODUCT_ID("REVIEW-020", "Product id is required", 400),
    INVALID_USER_ID("REVIEW-021", "User id is required", 400),
    INVALID_ORDER_ID("REVIEW-022", "Order id is required", 400),
    INVALID_REVIEW_IMAGE("REVIEW-023", "Invalid review image reference", 400),

    REVIEW_STORAGE_ERROR("REVIEW-999", "Review storage error", 500);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    ReviewErrorCode(String code, String defaultMessage, int httpStatus) {
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