package com.onatsubasi.finalcase.basket.presentation;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.core.exception.CommonErrorCode;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import com.onatsubasi.finalcase.common.core.response.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", ex.getErrorCode().code());

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn(
                    "Basket business exception occurred: errorCode={}, message={}",
                    ex.getErrorCode().code(),
                    ex.getMessage()
            );

            return ResponseEntity
                    .status(ex.getErrorCode().httpStatus())
                    .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), correlationId));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.VALIDATION_FAILED.code());

            List<FieldError> errors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                    .toList();

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn("Basket request validation failed: fieldErrorCount={}", errors.size());

            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.validation(CommonErrorCode.VALIDATION_FAILED, errors, correlationId));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.VALIDATION_FAILED.code());

            List<FieldError> errors = ex.getConstraintViolations()
                    .stream()
                    .map(error -> new FieldError(
                            error.getPropertyPath().toString(),
                            error.getMessage()
                    ))
                    .toList();

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn("Basket constraint validation failed: fieldErrorCount={}", errors.size());

            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.validation(CommonErrorCode.VALIDATION_FAILED, errors, correlationId));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.INTERNAL_SERVER_ERROR.code());

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.error("Unexpected basket-service error occurred", ex);

            return ResponseEntity
                    .internalServerError()
                    .body(ErrorResponse.of(
                            CommonErrorCode.INTERNAL_SERVER_ERROR,
                            "Internal server error",
                            correlationId
                    ));
        } finally {
            MDC.remove("errorCode");
        }
    }
}