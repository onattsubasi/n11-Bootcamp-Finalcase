package com.onatsubasi.finalcase.notification.application.dto.provider;

import java.util.Map;

public record NotificationProviderSendResult(
        boolean success,
        boolean retryable,
        String providerMessageId,
        String errorCode,
        String errorMessage,
        Map<String, Object> rawResponse
) {
}