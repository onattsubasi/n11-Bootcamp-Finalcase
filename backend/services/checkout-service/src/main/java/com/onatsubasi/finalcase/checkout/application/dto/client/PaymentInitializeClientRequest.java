package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInitializeClientRequest(
        UUID checkoutId,
        UUID orderId,
        UUID userId,
        BigDecimal amount,
        String currency,
        String provider,
        String methodType,
        String paymentToken,
        boolean useThreeDSecure
) {
}
