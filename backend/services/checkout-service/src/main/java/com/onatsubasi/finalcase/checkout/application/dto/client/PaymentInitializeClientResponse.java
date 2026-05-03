package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.UUID;

public record PaymentInitializeClientResponse(
        UUID paymentId,
        String paymentSessionId,
        String redirectUrl,
        String provider,
        String status
) {
}