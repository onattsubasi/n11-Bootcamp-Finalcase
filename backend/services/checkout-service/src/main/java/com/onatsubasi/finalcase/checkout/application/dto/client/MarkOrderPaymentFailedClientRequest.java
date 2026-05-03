package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.UUID;

public record MarkOrderPaymentFailedClientRequest(
        UUID paymentId,
        String paymentProvider,
        String paymentStatus,
        String providerTransactionId
) {
}