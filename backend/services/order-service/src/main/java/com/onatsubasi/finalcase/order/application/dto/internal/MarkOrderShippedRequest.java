package com.onatsubasi.finalcase.order.application.dto.internal;

import jakarta.validation.constraints.Size;

import java.time.Instant;

public record MarkOrderShippedRequest(
        @Size(max = 80, message = "Carrier cannot exceed 80 characters")
        String carrier,

        @Size(max = 150, message = "Tracking number cannot exceed 150 characters")
        String trackingNumber,

        Instant shippedAt
) {
}