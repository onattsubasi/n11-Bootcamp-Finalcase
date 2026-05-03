package com.onatsubasi.finalcase.checkout.application.dto.response;

import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;

import java.util.UUID;

public record CheckoutSubmitResponse(
        UUID checkoutSessionId,
        CheckoutStatus status,
        UUID orderId,
        String orderNumber,
        CheckoutPaymentActionResponse paymentAction,
        CheckoutMoneyBreakdownResponse money
) {
}