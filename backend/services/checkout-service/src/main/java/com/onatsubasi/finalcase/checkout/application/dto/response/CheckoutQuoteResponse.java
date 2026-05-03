package com.onatsubasi.finalcase.checkout.application.dto.response;

import java.util.List;
import java.util.UUID;

public record CheckoutQuoteResponse(
        UUID basketId,
        UUID userId,
        CheckoutMoneyBreakdownResponse money,
        List<CheckoutItemResponse> items,
        List<CheckoutDiscountResponse> discounts
) {
}
