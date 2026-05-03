package com.onatsubasi.finalcase.payment.application.dto.response;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentAttemptStatus;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Schema(description = "Payment attempt response")
public record PaymentAttemptResponse(
        UUID id,
        int attemptNumber,
        PaymentProviderCode provider,
        PaymentMethod method,
        PaymentAttemptStatus status,
        BigDecimal amount,
        BigDecimal paidAmount,
        String currency,
        String providerToken,
        String providerPaymentId,
        String providerTransactionId,
        String providerConversationId,
        String providerStatus,
        String paymentPageUrl,
        String failureReason,
        Instant createdAt,
        Instant completedAt
) {
}
