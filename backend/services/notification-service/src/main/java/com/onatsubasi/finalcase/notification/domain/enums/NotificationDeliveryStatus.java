package com.onatsubasi.finalcase.notification.domain.enums;

public enum NotificationDeliveryStatus {
    PENDING,
    SENT,
    FAILED,
    RETRY_SCHEDULED,
    GAVE_UP,
    SKIPPED
}