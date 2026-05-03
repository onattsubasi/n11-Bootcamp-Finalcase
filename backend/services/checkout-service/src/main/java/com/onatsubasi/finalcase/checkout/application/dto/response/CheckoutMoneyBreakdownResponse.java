package com.onatsubasi.finalcase.checkout.application.dto.response;

import java.math.BigDecimal;

public record CheckoutMoneyBreakdownResponse(
        BigDecimal subtotalAmount,
        BigDecimal itemDiscountAmount,
        BigDecimal promotionDiscountAmount,
        BigDecimal shippingFee,
        BigDecimal shippingDiscountAmount,
        BigDecimal taxAmount,
        BigDecimal grandTotalAmount,
        String currency
) {
}