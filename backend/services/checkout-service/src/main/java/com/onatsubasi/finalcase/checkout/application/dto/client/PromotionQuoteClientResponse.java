package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.List;

public record PromotionQuoteClientResponse(
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal shippingDiscount,
        BigDecimal grandTotal,
        List<AppliedPromotionDiscountClientResponse> appliedDiscounts
) {
}