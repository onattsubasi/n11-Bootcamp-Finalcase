package com.onatsubasi.finalcase.payment.support;

import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderAddressInfo;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderBasketItemInfo;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderBuyerInfo;
import com.onatsubasi.finalcase.payment.application.dto.request.InitializePaymentRequest;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.model.Payment;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class PaymentTestData {

    public static final UUID PAYMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID CHECKOUT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private PaymentTestData() {
    }

    public static Payment paymentWaitingProviderAction() {
        Payment payment = new Payment(
                CHECKOUT_ID,
                ORDER_ID,
                "ORD-20260501-000001",
                USER_ID,
                PaymentProviderCode.IYZICO,
                PaymentMethod.CHECKOUT_FORM,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "try"
        );
        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);
        payment.markWaitingProviderAction();
        return payment;
    }

    public static Payment succeededPayment() {
        Payment payment = paymentWaitingProviderAction();
        payment.markSucceeded("provider-payment-1", "provider-tx-1", "conversation-1", "SUCCESS");
        return payment;
    }

    public static InitializePaymentRequest initializePaymentRequest() {
        return new InitializePaymentRequest(
                CHECKOUT_ID,
                ORDER_ID,
                "ORD-20260501-000001",
                USER_ID,
                new BigDecimal("100.00"),
                "TRY",
                PaymentProviderCode.IYZICO,
                PaymentMethod.CHECKOUT_FORM,
                "https://app.example.com/success",
                "https://app.example.com/failure",
                "127.0.0.1",
                UUID.fromString("00000000-0000-0000-0000-000000000005"),
                ProviderBuyerInfo.builder()
                        .id(USER_ID.toString())
                        .name("Oytun")
                        .surname("Coban")
                        .email("oytun@example.com")
                        .phone("+905551112233")
                        .registrationAddress("Example address")
                        .city("Istanbul")
                        .country("Turkey")
                        .zipCode("34000")
                        .ip("127.0.0.1")
                        .build(),
                ProviderAddressInfo.builder()
                        .contactName("Oytun Coban")
                        .city("Istanbul")
                        .country("Turkey")
                        .address("Shipping address")
                        .zipCode("34000")
                        .build(),
                ProviderAddressInfo.builder()
                        .contactName("Oytun Coban")
                        .city("Istanbul")
                        .country("Turkey")
                        .address("Billing address")
                        .zipCode("34000")
                        .build(),
                List.of(ProviderBasketItemInfo.builder()
                        .id("product-1")
                        .name("Product 1")
                        .categoryName("General")
                        .itemType("PHYSICAL")
                        .price(new BigDecimal("100.00"))
                        .build())
        );
    }
}
