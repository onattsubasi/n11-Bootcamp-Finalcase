package com.onatsubasi.finalcase.order.application.dto.response;

import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatusChangeSource;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryResponse(
        UUID id,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        OrderStatusChangeSource source,
        String changedBy,
        String reason,
        Instant createdAt
) {
}