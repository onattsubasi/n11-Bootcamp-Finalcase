package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BasketSnapshotClientResponse(
        UUID basketId,
        UUID userId,
        List<BasketItemClientResponse> items
) {
    public BigDecimal subtotalAmount() {
        if (items == null) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String currency() {
        if (items == null || items.isEmpty()) {
            return null;
        }

        return items.stream()
                .map(BasketItemClientResponse::currency)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }
}
