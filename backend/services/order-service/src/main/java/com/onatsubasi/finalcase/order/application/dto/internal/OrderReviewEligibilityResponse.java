package com.onatsubasi.finalcase.order.application.dto.internal;

import java.util.UUID;

public record OrderReviewEligibilityResponse(
        UUID orderId,
        UUID orderItemId,
        String orderNumber,
        UUID userId,
        String productId,
        boolean eligible,
        String reason
) {
}