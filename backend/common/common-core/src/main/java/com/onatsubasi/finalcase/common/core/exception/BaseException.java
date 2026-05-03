package com.onatsubasi.finalcase.common.core.exception;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public BaseException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage(), null, Collections.emptyMap());
    }

    public BaseException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, Collections.emptyMap());
    }

    public BaseException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, errorCode.defaultMessage(), cause, Collections.emptyMap());
    }

    public BaseException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, Collections.emptyMap());
    }

    public BaseException(ErrorCode errorCode, String message, Map<String, Object> details) {
        this(errorCode, message, null, details);
    }

    public BaseException(
            ErrorCode errorCode,
            String message,
            Throwable cause,
            Map<String, Object> details
    ) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
        this.details = details == null ? Collections.emptyMap() : Map.copyOf(details);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return errorCode.httpStatus();
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}