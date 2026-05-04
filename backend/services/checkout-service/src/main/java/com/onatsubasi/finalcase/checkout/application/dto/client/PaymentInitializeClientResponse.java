package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentInitializeClientResponse(
        UUID paymentId,
        @JsonAlias("paymentAttemptId")
        String paymentSessionId,
        @JsonAlias({"paymentPageUrl", "checkoutFormContent"})
        String redirectUrl,
        String provider,
        String status
) {
}
