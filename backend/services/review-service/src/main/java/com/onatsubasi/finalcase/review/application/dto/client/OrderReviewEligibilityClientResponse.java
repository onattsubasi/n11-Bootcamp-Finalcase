package com.onatsubasi.finalcase.review.application.dto.client;

import java.util.UUID;

public record OrderReviewEligibilityClientResponse(
        UUID orderId,
        UUID orderItemId,
        String orderNumber,
        UUID userId,
        String productId,
        boolean eligible,
        String reason
) {
}