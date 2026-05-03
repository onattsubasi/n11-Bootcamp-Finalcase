package com.onatsubasi.finalcase.order.application.dto.internal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MarkOrderPaidRequest(
        @NotNull(message = "Payment id is required")
        UUID paymentId,

        @Size(max = 50, message = "Payment provider cannot exceed 50 characters")
        String paymentProvider,

        @Size(max = 50, message = "Payment status cannot exceed 50 characters")
        String paymentStatus,

        @Size(max = 150, message = "Provider transaction id cannot exceed 150 characters")
        String providerTransactionId
) {
}
