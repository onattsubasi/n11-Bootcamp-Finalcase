package com.onatsubasi.finalcase.user.presentation.exception;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.core.exception.CommonErrorCode;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import com.onatsubasi.finalcase.common.core.response.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
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
                    "User business exception occurred, errorCode={}, message={}",
                    ex.getErrorCode().code(),
                    ex.getMessage()
            );

            return ResponseEntity
                    .status(ex.getErrorCode().httpStatus())
                    .body(ErrorResponse.of(
                            ex.getErrorCode(),
                            ex.getMessage(),
                            correlationId
                    ));
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
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getDefaultMessage()
                    ))
                    .toList();

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn("User request validation failed, fieldErrorCount={}", errors.size());

            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.validation(
                            CommonErrorCode.VALIDATION_FAILED,
                            errors,
                            correlationId
                    ));
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
                    .map(violation -> new FieldError(
                            violation.getPropertyPath().toString(),
                            violation.getMessage()
                    ))
                    .toList();

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn("User constraint validation failed, fieldErrorCount={}", errors.size());

            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.validation(
                            CommonErrorCode.VALIDATION_FAILED,
                            errors,
                            correlationId
                    ));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.MALFORMED_REQUEST.code());

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn("User malformed request body: {}", ex.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.of(
                            CommonErrorCode.MALFORMED_REQUEST,
                            "Malformed request body",
                            correlationId
                    ));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.VALIDATION_FAILED.code());

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);
            String message = "Invalid value for parameter: " + ex.getName();

            log.warn(
                    "User request parameter type mismatch, parameter={}, requiredType={}",
                    ex.getName(),
                    ex.getRequiredType() == null ? null : ex.getRequiredType().getSimpleName()
            );

            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.of(
                            CommonErrorCode.VALIDATION_FAILED,
                            message,
                            correlationId
                    ));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.RESOURCE_CONFLICT.code());

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn(
                    "User data integrity violation occurred: {}",
                    ex.getMostSpecificCause().getMessage()
            );

            return ResponseEntity
                    .status(CommonErrorCode.RESOURCE_CONFLICT.httpStatus())
                    .body(ErrorResponse.of(
                            CommonErrorCode.RESOURCE_CONFLICT,
                            "User data conflict",
                            correlationId
                    ));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.UNAUTHORIZED.code());

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn("User authentication exception occurred: {}", ex.getMessage());

            return ResponseEntity
                    .status(CommonErrorCode.UNAUTHORIZED.httpStatus())
                    .body(ErrorResponse.of(
                            CommonErrorCode.UNAUTHORIZED,
                            CommonErrorCode.UNAUTHORIZED.defaultMessage(),
                            correlationId
                    ));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.FORBIDDEN.code());

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn("User access denied exception occurred: {}", ex.getMessage());

            return ResponseEntity
                    .status(CommonErrorCode.FORBIDDEN.httpStatus())
                    .body(ErrorResponse.of(
                            CommonErrorCode.FORBIDDEN,
                            CommonErrorCode.FORBIDDEN.defaultMessage(),
                            correlationId
                    ));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.METHOD_NOT_ALLOWED.code());

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn("User method not allowed: {}", ex.getMessage());

            return ResponseEntity
                    .status(CommonErrorCode.METHOD_NOT_ALLOWED.httpStatus())
                    .body(ErrorResponse.of(
                            CommonErrorCode.METHOD_NOT_ALLOWED,
                            CommonErrorCode.METHOD_NOT_ALLOWED.defaultMessage(),
                            correlationId
                    ));
        } finally {
            MDC.remove("errorCode");
        }
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        try {
            MDC.put("errorCode", CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.code());

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            log.warn("User unsupported media type: {}", ex.getMessage());

            return ResponseEntity
                    .status(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.httpStatus())
                    .body(ErrorResponse.of(
                            CommonErrorCode.UNSUPPORTED_MEDIA_TYPE,
                            CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.defaultMessage(),
                            correlationId
                    ));
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

            log.error("Unexpected user-service error occurred", ex);

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