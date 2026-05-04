package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;

public record PaymentBasketItemClientRequest(
        String id,
        String name,
        String categoryName,
        String itemType,
        BigDecimal price
) {
}
