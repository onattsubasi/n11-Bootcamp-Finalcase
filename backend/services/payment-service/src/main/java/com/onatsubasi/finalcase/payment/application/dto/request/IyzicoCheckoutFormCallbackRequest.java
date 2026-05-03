package com.onatsubasi.finalcase.payment.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Iyzico Checkout Form callback payload")
public record IyzicoCheckoutFormCallbackRequest(
        @NotBlank(message = "Iyzico token is required")
        String token,

        String status,

        String conversationId
) {
}