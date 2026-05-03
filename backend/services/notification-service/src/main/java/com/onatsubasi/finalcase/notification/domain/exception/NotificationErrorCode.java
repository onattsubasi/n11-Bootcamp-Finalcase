package com.onatsubasi.finalcase.notification.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_NOT_FOUND("NOTIFICATION-001", "Notification not found", 404),
    NOTIFICATION_ACCESS_DENIED("NOTIFICATION-002", "Notification access denied", 403),

    NOTIFICATION_TEMPLATE_NOT_FOUND("NOTIFICATION-003", "Notification template not found", 404),
    NOTIFICATION_TEMPLATE_INACTIVE("NOTIFICATION-004", "Notification template is inactive", 409),
    NOTIFICATION_TEMPLATE_RENDER_FAILED("NOTIFICATION-005", "Notification template render failed", 409),
    NOTIFICATION_TEMPLATE_VARIABLE_MISSING("NOTIFICATION-006", "Required template variable is missing", 400),

    NOTIFICATION_DELIVERY_NOT_FOUND("NOTIFICATION-007", "Notification delivery not found", 404),
    NOTIFICATION_DELIVERY_NOT_RETRYABLE("NOTIFICATION-008", "Notification delivery is not retryable", 409),
    NOTIFICATION_DELIVERY_FAILED("NOTIFICATION-009", "Notification delivery failed", 409),
    NOTIFICATION_PROVIDER_NOT_SUPPORTED("NOTIFICATION-010", "Notification provider is not supported", 400),
    NOTIFICATION_CHANNEL_NOT_SUPPORTED("NOTIFICATION-011", "Notification channel is not supported", 400),

    NOTIFICATION_EVENT_INVALID("NOTIFICATION-012", "Notification event is invalid", 400),
    NOTIFICATION_EVENT_ALREADY_PROCESSED("NOTIFICATION-013", "Notification event already processed", 409),
    NOTIFICATION_EVENT_UNSUPPORTED("NOTIFICATION-014", "Notification event type is unsupported", 400),

    INVALID_NOTIFICATION_DATA("NOTIFICATION-015", "Invalid notification data", 400),
    INVALID_RECIPIENT("NOTIFICATION-016", "Invalid notification recipient", 400),

    NOTIFICATION_PREFERENCE_NOT_FOUND("NOTIFICATION-017", "Notification preference not found", 404),
    USER_PRODUCT_INTEREST_NOT_FOUND("NOTIFICATION-018", "User product interest not found", 404);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    NotificationErrorCode(String code, String defaultMessage, int httpStatus) {
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