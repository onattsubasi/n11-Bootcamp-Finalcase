package com.onatsubasi.finalcase.shipment.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to cancel shipment")
public record CancelShipmentRequest(
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}
