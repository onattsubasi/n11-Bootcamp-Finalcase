package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentInitializeClientRequest(
        UUID checkoutId,
        UUID orderId,
        String orderNumber,
        UUID userId,
        BigDecimal amount,
        String currency,
        String provider,
        String method,
        String successUrl,
        String failureUrl,
        String clientIp,
        UUID basketId,
        PaymentBuyerClientRequest buyer,
        PaymentAddressClientRequest shippingAddress,
        PaymentAddressClientRequest billingAddress,
        List<PaymentBasketItemClientRequest> basketItems
) {
}
