package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PromotionQuoteClientRequest(
        UUID userId,
        String couponCode,
        List<PromotionQuoteItemClientRequest> items,
        BigDecimal shippingFee,
        String currency
) {
}
