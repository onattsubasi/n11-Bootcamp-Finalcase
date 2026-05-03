package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.promotion.application.dto.internal.PromotionQuoteLineRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DiscountCalculationContext(
        UUID userId,
        List<PromotionQuoteLineRequest> items,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        String currency
) {
}
