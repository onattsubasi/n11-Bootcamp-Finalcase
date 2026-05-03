package com.onatsubasi.finalcase.order.application.dto.response;

import java.util.UUID;

public record OrderPaymentSummaryResponse(
        UUID paymentId,
        String paymentProvider,
        String paymentStatus,
        String providerTransactionId
) {
}