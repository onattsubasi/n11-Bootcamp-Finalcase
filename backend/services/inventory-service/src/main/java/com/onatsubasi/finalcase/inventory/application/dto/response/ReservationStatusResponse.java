package com.onatsubasi.finalcase.inventory.application.dto.response;

import com.onatsubasi.finalcase.inventory.domain.enums.ReleaseReason;
import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Reservation status response")
public record ReservationStatusResponse(
        UUID reservationId,
        UUID checkoutId,
        UUID orderId,
        StockReservationStatus status,
        Instant reservedUntil,
        Instant confirmedAt,
        Instant releasedAt,
        ReleaseReason releaseReason
) {
}
