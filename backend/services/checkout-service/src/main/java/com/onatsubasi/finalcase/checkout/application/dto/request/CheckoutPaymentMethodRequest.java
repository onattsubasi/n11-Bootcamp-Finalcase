package com.onatsubasi.finalcase.checkout.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutPaymentMethodRequest(
        @NotBlank(message = "Payment provider is required")
        @Size(max = 50, message = "Payment provider cannot exceed 50 characters")
        String provider,

        @NotBlank(message = "Payment method type is required")
        @Size(max = 50, message = "Payment method type cannot exceed 50 characters")
        String methodType,

        @Size(max = 200, message = "Payment token cannot exceed 200 characters")
        String paymentToken,

        Boolean useThreeDSecure
) {
    public boolean shouldUseThreeDSecure() {
        return Boolean.TRUE.equals(useThreeDSecure);
    }
}