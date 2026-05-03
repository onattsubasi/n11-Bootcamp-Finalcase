package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BasketSnapshotClientResponse(
        UUID basketId,
        UUID userId,
        List<BasketItemClientResponse> items,
        BigDecimal subtotalAmount,
        String currency
) {
}