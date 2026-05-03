package com.onatsubasi.finalcase.payment.application.dto.provider;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ProviderCancelCommand(
        UUID paymentId,
        UUID cancellationId,
        String providerPaymentId,
        String providerTransactionId,
        String reason
) {
}