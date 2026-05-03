package com.onatsubasi.finalcase.shipment.application.dto.response;

import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatusChangeSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Shipment status history response")
public record ShipmentStatusHistoryResponse(
        UUID id,
        ShipmentStatus fromStatus,
        ShipmentStatus toStatus,
        ShipmentStatusChangeSource source,
        String changedBy,
        String reason,
        Instant createdAt
) {
}