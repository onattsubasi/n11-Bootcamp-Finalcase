package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.UUID;

public record PromotionQuoteItemClientRequest(
        UUID productId,
        UUID categoryId,
        UUID brandId,
        BigDecimal unitPrice,
        int quantity
) {
}
