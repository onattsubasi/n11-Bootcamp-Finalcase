package com.onatsubasi.finalcase.payment.application.dto.event;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentResultEvent(
        UUID paymentId,
        UUID checkoutId,
        UUID orderId,
        UUID userId,
        PaymentProviderCode provider,
        PaymentMethod method,
        PaymentStatus paymentStatus,
        String providerPaymentId,
        String providerTransactionId,
        String providerConversationId,
        BigDecimal amount,
        BigDecimal paidAmount,
        String currency,
        String failureReason
) {
}
