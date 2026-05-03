package com.onatsubasi.finalcase.order.application.dto.request;

import jakarta.validation.constraints.Size;

public record CancelOrderRequest(
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}