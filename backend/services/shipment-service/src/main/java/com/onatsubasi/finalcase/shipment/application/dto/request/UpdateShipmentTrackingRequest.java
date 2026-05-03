package com.onatsubasi.finalcase.shipment.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update shipment tracking information")
public record UpdateShipmentTrackingRequest(
        @Size(max = 150, message = "Tracking number cannot exceed 150 characters")
        String trackingNumber,

        @Size(max = 1000, message = "Tracking URL cannot exceed 1000 characters")
        String trackingUrl,

        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}