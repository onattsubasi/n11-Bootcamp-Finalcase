package com.onatsubasi.finalcase.payment.application.dto.provider;

import lombok.Builder;

import java.util.Map;

@Builder
public record ProviderCancelResult(
        boolean success,
        String providerCancelId,
        String providerStatus,
        String failureReason,
        Map<String, Object> providerResponseSnapshot
) {
}