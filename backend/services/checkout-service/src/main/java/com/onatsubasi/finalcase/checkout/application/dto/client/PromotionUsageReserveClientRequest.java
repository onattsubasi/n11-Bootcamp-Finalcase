package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PromotionUsageReserveClientRequest(
        UUID checkoutId,
        UUID userId,
        String couponCode,
        List<UUID> selectedPromotionIds,
        List<PromotionQuoteItemClientRequest> items,
        BigDecimal shippingFee,
        String currency
) {
}
