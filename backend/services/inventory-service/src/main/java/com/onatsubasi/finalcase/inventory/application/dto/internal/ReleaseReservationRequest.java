package com.onatsubasi.finalcase.inventory.application.dto.internal;

import com.onatsubasi.finalcase.inventory.domain.enums.ReleaseReason;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Internal request to release a stock reservation after payment/checkout failure")
public record ReleaseReservationRequest(

        @Schema(description = "Reason for releasing the reservation", example = "PAYMENT_FAILED")
        ReleaseReason reason
) {
}
