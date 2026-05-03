package com.onatsubasi.finalcase.order.application.dto.response;

import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        String orderNumber,
        UUID checkoutId,
        UUID userId,
        OrderStatus status,
        BigDecimal grandTotalAmount,
        String currency,
        Instant createdAt
) {
}