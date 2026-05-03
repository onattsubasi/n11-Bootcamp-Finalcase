package com.onatsubasi.finalcase.common.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        int status,
        String errorCode,
        String message,
        String correlationId,
        Instant timestamp,
        List<FieldError> errors
) {

    public ErrorResponse {
        success = false;
        timestamp = timestamp == null ? Instant.now() : timestamp;
        errors = errors == null || errors.isEmpty() ? null : List.copyOf(errors);
    }

    public static ErrorResponse of(ErrorCode errorCode, String correlationId) {
        return new ErrorResponse(
                false,
                errorCode.httpStatus(),
                errorCode.code(),
                errorCode.defaultMessage(),
                correlationId,
                Instant.now(),
                null
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String correlationId) {
        return new ErrorResponse(
                false,
                errorCode.httpStatus(),
                errorCode.code(),
                message,
                correlationId,
                Instant.now(),
                null
        );
    }

    public static ErrorResponse validation(
            ErrorCode errorCode,
            List<FieldError> errors,
            String correlationId
    ) {
        return new ErrorResponse(
                false,
                errorCode.httpStatus(),
                errorCode.code(),
                errorCode.defaultMessage(),
                correlationId,
                Instant.now(),
                errors
        );
    }
}