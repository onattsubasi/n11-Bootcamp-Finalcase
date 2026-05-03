package com.onatsubasi.finalcase.payment.application.dto.request;

import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderAddressInfo;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderBasketItemInfo;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderBuyerInfo;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Internal request used by Checkout Service to initialize a payment")
public record InitializePaymentRequest(
        @NotNull(message = "Checkout id is required")
        UUID checkoutId,

        @NotNull(message = "Order id is required")
        UUID orderId,

        @NotBlank(message = "Order number is required")
        @Size(max = 80, message = "Order number cannot exceed 80 characters")
        String orderNumber,

        @NotNull(message = "User id is required")
        UUID userId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        String currency,

        @NotNull(message = "Provider is required")
        PaymentProviderCode provider,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        String successUrl,
        String failureUrl,
        String clientIp,
        UUID basketId,

        @NotNull(message = "Buyer info is required")
        ProviderBuyerInfo buyer,

        @NotNull(message = "Shipping address is required")
        ProviderAddressInfo shippingAddress,

        @NotNull(message = "Billing address is required")
        ProviderAddressInfo billingAddress,

        @NotNull(message = "Basket items are required")
        @Size(min = 1, message = "At least one basket item is required")
        List<ProviderBasketItemInfo> basketItems
) {
}