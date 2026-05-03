package com.onatsubasi.finalcase.auth.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum AuthErrorCode implements ErrorCode {

    INVALID_CREDENTIALS("AUTH-001", "Invalid email or password", 401),
    USER_ALREADY_EXISTS("AUTH-002", "User already exists", 409),
    USER_NOT_FOUND("AUTH-003", "User not found", 404),
    USER_DISABLED("AUTH-004", "User account is disabled", 403),
    INVALID_REFRESH_TOKEN("AUTH-005", "Invalid refresh token", 401),
    REFRESH_TOKEN_EXPIRED("AUTH-006", "Refresh token expired", 401),
    REFRESH_TOKEN_REUSED("AUTH-007", "Refresh token reuse detected", 401),
    AUTHENTICATION_REQUIRED("AUTH-008", "Authentication is required", 401),
    CURRENT_PASSWORD_INVALID("AUTH-009", "Current password is invalid", 401);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    AuthErrorCode(String code, String defaultMessage, int httpStatus) {
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
