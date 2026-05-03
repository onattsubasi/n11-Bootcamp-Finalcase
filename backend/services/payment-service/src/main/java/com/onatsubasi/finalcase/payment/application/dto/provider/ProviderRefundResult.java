package com.onatsubasi.finalcase.payment.application.dto.provider;

import lombok.Builder;

import java.util.Map;

@Builder
public record ProviderRefundResult(
        boolean success,
        String providerRefundId,
        String providerStatus,
        String failureReason,
        Map<String, Object> providerResponseSnapshot
) {
}