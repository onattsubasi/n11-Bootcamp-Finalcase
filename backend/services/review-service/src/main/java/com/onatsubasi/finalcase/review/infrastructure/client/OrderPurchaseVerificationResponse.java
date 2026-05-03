package com.onatsubasi.finalcase.review.infrastructure.client;

import java.time.Instant;
import java.util.UUID;

public record OrderPurchaseVerificationResponse(
        boolean verified,
        UUID orderId,
        UUID orderItemId,
        String orderNumber,
        Instant deliveredAt
) {
}
