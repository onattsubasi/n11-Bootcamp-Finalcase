package com.onatsubasi.finalcase.payment.application.dto.response;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentAttemptStatus;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payment initialization response")
public record PaymentInitializeResponse(
        UUID paymentId,
        UUID paymentAttemptId,
        UUID orderId,
        UUID checkoutId,
        PaymentProviderCode provider,
        PaymentMethod method,
        PaymentStatus status,
        PaymentAttemptStatus attemptStatus,
        String providerToken,
        String paymentPageUrl,
        String checkoutFormContent,
        BigDecimal amount,
        BigDecimal paidAmount,
        String currency
) {
}