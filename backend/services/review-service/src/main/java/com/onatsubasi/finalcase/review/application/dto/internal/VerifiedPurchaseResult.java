package com.onatsubasi.finalcase.review.application.dto.internal;

import java.time.Instant;
import java.util.UUID;

public record VerifiedPurchaseResult(
        boolean verified,
        UUID orderId,
        UUID orderItemId,
        String orderNumber,
        Instant deliveredAt
) {
}
