package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PromotionQuoteClientResponse(
        BigDecimal subtotal,
        @JsonAlias("totalDiscountAmount")
        BigDecimal totalDiscount,
        @JsonAlias("shippingDiscountAmount")
        BigDecimal shippingDiscount,
        @JsonAlias("payableAmount")
        BigDecimal grandTotal,
        @JsonAlias("selectedDiscounts")
        List<AppliedPromotionDiscountClientResponse> appliedDiscounts
) {
}
