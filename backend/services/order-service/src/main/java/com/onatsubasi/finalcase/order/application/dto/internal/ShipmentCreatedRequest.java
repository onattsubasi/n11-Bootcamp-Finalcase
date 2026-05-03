package com.onatsubasi.finalcase.order.application.dto.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ShipmentCreatedRequest(
        @NotNull(message = "Shipment id is required")
        UUID shipmentId,

        @NotBlank(message = "Shipment number is required")
        @Size(max = 80, message = "Shipment number cannot exceed 80 characters")
        String shipmentNumber,

        @Size(max = 80, message = "Carrier cannot exceed 80 characters")
        String carrier,

        @Size(max = 150, message = "Tracking number cannot exceed 150 characters")
        String trackingNumber,

        @Size(max = 50, message = "Shipment status cannot exceed 50 characters")
        String shipmentStatus
) {
}
