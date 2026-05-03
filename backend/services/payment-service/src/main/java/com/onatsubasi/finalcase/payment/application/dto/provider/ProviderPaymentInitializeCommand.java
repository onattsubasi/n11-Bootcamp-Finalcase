package com.onatsubasi.finalcase.payment.application.dto.provider;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record ProviderPaymentInitializeCommand(
        UUID paymentId,
        UUID paymentAttemptId,
        UUID checkoutId,
        UUID orderId,
        String orderNumber,
        UUID userId,
        BigDecimal amount,
        BigDecimal paidAmount,
        String currency,
        PaymentProviderCode provider,
        PaymentMethod method,
        String successUrl,
        String failureUrl,
        String callbackUrl,
        String clientIp,
        UUID basketId,
        ProviderBuyerInfo buyer,
        ProviderAddressInfo shippingAddress,
        ProviderAddressInfo billingAddress,
        List<ProviderBasketItemInfo> basketItems
) {
}
