package com.onatsubasi.finalcase.order.application.dto.internal;

import java.time.Instant;

public record MarkOrderDeliveredRequest(
        Instant deliveredAt
) {
}