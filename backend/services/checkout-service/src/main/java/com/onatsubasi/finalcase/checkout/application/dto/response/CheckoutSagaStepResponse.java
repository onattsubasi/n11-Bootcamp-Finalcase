package com.onatsubasi.finalcase.checkout.application.dto.response;

import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepName;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepStatus;

import java.time.Instant;
import java.util.UUID;

public record CheckoutSagaStepResponse(
        UUID id,
        CheckoutSagaStepName stepName,
        CheckoutSagaStepStatus status,
        String errorMessage,
        Instant startedAt,
        Instant completedAt
) {
}