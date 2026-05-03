package com.onatsubasi.finalcase.shipment.application.dto.client;

import java.time.Instant;

public record MarkOrderDeliveredClientRequest(
        Instant deliveredAt
) {
}