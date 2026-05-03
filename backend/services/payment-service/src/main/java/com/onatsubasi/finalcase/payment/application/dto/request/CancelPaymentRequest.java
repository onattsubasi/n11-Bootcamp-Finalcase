package com.onatsubasi.finalcase.payment.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to cancel a payment before settlement")
public record CancelPaymentRequest(
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}