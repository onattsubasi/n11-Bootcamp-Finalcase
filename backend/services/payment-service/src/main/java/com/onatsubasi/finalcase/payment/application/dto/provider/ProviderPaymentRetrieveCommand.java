package com.onatsubasi.finalcase.payment.application.dto.provider;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProviderPaymentRetrieveCommand(
        PaymentProviderCode provider,
        PaymentMethod method,
        String providerToken,
        UUID paymentId,
        UUID paymentAttemptId
) {
}