package com.onatsubasi.finalcase.common.core.exception;

public enum CommonErrorCode implements ErrorCode {

    VALIDATION_FAILED("COMMON-001", "Request validation failed", 400),
    MALFORMED_REQUEST("COMMON-002", "Malformed request body", 400),
    UNAUTHORIZED("COMMON-003", "Authentication is required", 401),
    FORBIDDEN("COMMON-004", "Access denied", 403),
    RESOURCE_NOT_FOUND("COMMON-005", "Resource not found", 404),
    METHOD_NOT_ALLOWED("COMMON-008", "Method not allowed", 405),
    UNSUPPORTED_MEDIA_TYPE("COMMON-009", "Unsupported media type", 415),
    RESOURCE_CONFLICT("COMMON-006", "Resource conflict", 409),
    TOO_MANY_REQUESTS("COMMON-007", "Too many requests", 429),
    INTERNAL_SERVER_ERROR("COMMON-999", "Internal server error", 500),
    SERVICE_UNAVAILABLE("COMMON-998", "Service unavailable", 503);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    CommonErrorCode(String code, String defaultMessage, int httpStatus) {
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