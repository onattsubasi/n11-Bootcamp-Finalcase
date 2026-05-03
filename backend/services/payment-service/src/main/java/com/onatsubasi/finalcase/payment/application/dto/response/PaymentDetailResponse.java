package com.onatsubasi.finalcase.payment.application.dto.response;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Payment detail response")
public record PaymentDetailResponse(
        UUID id,
        UUID checkoutId,
        UUID orderId,
        String orderNumber,
        UUID userId,
        PaymentProviderCode provider,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal refundedAmount,
        String currency,
        String providerPaymentId,
        String providerTransactionId,
        String providerConversationId,
        String providerStatus,
        String failureReason,
        List<PaymentAttemptResponse> attempts,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {
}