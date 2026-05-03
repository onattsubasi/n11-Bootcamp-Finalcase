package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;

public record BasketItemClientResponse(
        String productId,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        String currency
) {
}