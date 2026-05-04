package com.onatsubasi.finalcase.order.application.dto.internal;

import java.time.Instant;
import java.util.UUID;

public record VerifyPurchaseInternalResponse(
        boolean verified,
        UUID orderId,
        UUID orderItemId,
        String orderNumber,
        Instant deliveredAt
) {
    public static VerifyPurchaseInternalResponse verified(
            UUID orderId,
            UUID orderItemId,
            String orderNumber,
            Instant deliveredAt
    ) {
        return new VerifyPurchaseInternalResponse(
                true,
                orderId,
                orderItemId,
                orderNumber,
                deliveredAt
        );
    }

    public static VerifyPurchaseInternalResponse notVerified() {
        return new VerifyPurchaseInternalResponse(
                false,
                null,
                null,
                null,
                null
        );
    }
}