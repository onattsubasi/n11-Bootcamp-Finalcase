package com.onatsubasi.finalcase.promotion.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromotionQuoteResponse(
        UUID userId,
        String couponCode,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal totalDiscountAmount,
        BigDecimal shippingDiscountAmount,
        BigDecimal payableAmount,
        String currency,
        List<AppliedDiscountResponse> eligibleDiscounts,
        List<AppliedDiscountResponse> selectedDiscounts,
        List<String> ineligibleReasons,
        Instant quotedAt
) {
}