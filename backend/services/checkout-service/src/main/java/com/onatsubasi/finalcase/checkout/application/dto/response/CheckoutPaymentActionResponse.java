package com.onatsubasi.finalcase.checkout.application.dto.response;

import java.util.UUID;

public record CheckoutPaymentActionResponse(
        UUID paymentId,
        String paymentSessionId,
        String redirectUrl,
        String provider,
        String status
) {
}