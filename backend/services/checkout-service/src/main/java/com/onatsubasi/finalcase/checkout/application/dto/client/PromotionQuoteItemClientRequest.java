package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;

public record PromotionQuoteItemClientRequest(
        String productId,
        String categoryId,
        String brandId,
        BigDecimal unitPrice,
        int quantity
) {
}