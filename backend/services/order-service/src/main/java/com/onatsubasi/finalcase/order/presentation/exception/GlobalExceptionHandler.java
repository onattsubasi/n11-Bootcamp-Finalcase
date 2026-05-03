package com.onatsubasi.finalcase.order.presentation.exception;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.core.exception.CommonErrorCode;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import com.onatsubasi.finalcase.common.core.response.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        String correlationId = correlationId(request);
        log.warn("Order service business error errorCode={} correlationId={} message={}", ex.getErrorCode().code(), correlationId, ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), correlationId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        String correlationId = correlationId(request);
        log.warn("Order service validation error correlationId={} fieldCount={}", correlationId, errors.size());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.validation(CommonErrorCode.VALIDATION_FAILED, errors, correlationId));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new FieldError(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        String correlationId = correlationId(request);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.validation(CommonErrorCode.VALIDATION_FAILED, errors, correlationId));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String correlationId = correlationId(request);
        log.debug("Malformed order service request body correlationId={}", correlationId, ex);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(CommonErrorCode.MALFORMED_REQUEST, "Malformed request body", correlationId));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String correlationId = correlationId(request);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(CommonErrorCode.VALIDATION_FAILED, "Invalid value for parameter: " + ex.getName(), correlationId));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String correlationId = correlationId(request);
        log.warn("Order service data integrity violation correlationId={}", correlationId, ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(CommonErrorCode.RESOURCE_CONFLICT, "Order data conflict", correlationId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        String correlationId = correlationId(request);
        log.error("Unexpected order service error correlationId={}", correlationId, ex);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR, "Internal server error", correlationId));
    }

    private String correlationId(HttpServletRequest request) {
        return request.getHeader(PlatformHeaders.X_CORRELATION_ID);
    }
}
