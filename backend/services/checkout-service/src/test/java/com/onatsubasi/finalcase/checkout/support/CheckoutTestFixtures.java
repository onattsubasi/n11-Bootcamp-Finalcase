package com.onatsubasi.finalcase.checkout.support;

import com.onatsubasi.finalcase.checkout.application.dto.client.*;
import com.onatsubasi.finalcase.checkout.application.dto.event.PaymentResultEventMessage;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutPaymentMethodRequest;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutQuoteRequest;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutSubmitRequest;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutMoneyBreakdownResponse;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutPaymentActionResponse;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSubmitResponse;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;
import com.onatsubasi.finalcase.common.security.UserContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CheckoutTestFixtures {

    public static final String CURRENCY = "TRY";

    private CheckoutTestFixtures() {
    }

    public static UserContext customer(UUID userId) {
        return new UserContext(userId, "customer@example.com", Set.of("CUSTOMER"));
    }

    public static CheckoutQuoteRequest quoteRequest(UUID basketId, UUID shippingAddressId) {
        return new CheckoutQuoteRequest(basketId, shippingAddressId, null, null);
    }

    public static CheckoutSubmitRequest submitRequest(UUID basketId, UUID shippingAddressId) {
        return new CheckoutSubmitRequest(
                basketId,
                shippingAddressId,
                null,
                null,
                new CheckoutPaymentMethodRequest("IYZICO", "CHECKOUT_FORM", null, false));
    }

    public static BasketSnapshotClientResponse basket(UUID userId, UUID basketId, String productId, String unitPrice,
            int quantity) {
        BigDecimal price = money(unitPrice);
        return new BasketSnapshotClientResponse(
                basketId,
                userId,
                List.of(new BasketItemClientResponse(productId, "SKU-1", quantity, price, CURRENCY)),
                price.multiply(BigDecimal.valueOf(quantity)),
                CURRENCY);
    }

    public static BasketSnapshotClientResponse emptyBasket(UUID userId, UUID basketId) {
        return new BasketSnapshotClientResponse(basketId, userId, List.of(), BigDecimal.ZERO, CURRENCY);
    }

    public static CatalogProductSnapshotClientResponse product(String productId, String price, boolean active) {
        return new CatalogProductSnapshotClientResponse(
                productId,
                "SKU-1",
                "example-product",
                "Example Product",
                "Description",
                UUID.randomUUID().toString(),
                "Brand",
                UUID.randomUUID().toString(),
                "Category",
                "https://cdn.example.com/product.jpg",
                money(price),
                CURRENCY,
                active);
    }

    public static PromotionQuoteClientResponse noDiscountQuote(String subtotal, String shippingFee) {
        BigDecimal subtotalAmount = money(subtotal);
        BigDecimal shippingAmount = money(shippingFee);
        return new PromotionQuoteClientResponse(
                subtotalAmount,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                subtotalAmount.add(shippingAmount),
                List.of());
    }

    public static PromotionQuoteClientResponse discountQuote(String subtotal, String discount, String grandTotal) {
        return new PromotionQuoteClientResponse(
                money(subtotal),
                money(discount),
                BigDecimal.ZERO,
                money(grandTotal),
                List.of(new AppliedPromotionDiscountClientResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "WELCOME",
                        "FIXED_AMOUNT_DISCOUNT",
                        money(discount),
                        BigDecimal.ZERO,
                        "welcome discount")));
    }

    public static UserAddressSnapshotClientResponse address(UUID addressId, UUID userId) {
        return new UserAddressSnapshotClientResponse(
                addressId,
                userId,
                "Home",
                "Oytun Coban",
                "+905551112233",
                "Türkiye",
                "İstanbul",
                "Kadıköy",
                "Caferağa",
                "Example street no 1",
                "Floor 2",
                "34710");
    }

    public static CheckoutSession paymentPendingSession(UUID userId, UUID basketId, UUID paymentId, UUID orderId) {
        CheckoutSession session = CheckoutSession.start(
                userId,
                "idem-" + UUID.randomUUID(),
                "hash-" + UUID.randomUUID(),
                CURRENCY,
                Instant.now().plusSeconds(1800));
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        session.attachBasket(basketId);
        session.attachInventoryReservation(UUID.randomUUID());
        session.attachOrder(orderId, "ORD-20260503-000001");
        session.attachPromotionUsageReservation(UUID.randomUUID());
        session.attachPaymentAction(paymentId, UUID.randomUUID(), "session-token", "https://pay.example.com",
                java.util.Map.of());
        return session;
    }

    public static PaymentResultEventMessage paymentSucceededEvent(UUID paymentId, UUID checkoutId, UUID orderId,
            UUID userId) {
        return new PaymentResultEventMessage(
                paymentId,
                checkoutId,
                orderId,
                userId,
                "IYZICO",
                "SUCCEEDED",
                "provider-tx-1",
                money("1900.00"),
                CURRENCY,
                null);
    }

    public static PaymentResultEventMessage paymentFailedEvent(UUID paymentId, UUID checkoutId, UUID orderId,
            UUID userId) {
        return new PaymentResultEventMessage(
                paymentId,
                checkoutId,
                orderId,
                userId,
                "IYZICO",
                "FAILED",
                "provider-tx-1",
                money("1900.00"),
                CURRENCY,
                "insufficient funds");
    }

    public static ShipmentClientResponse shipment(UUID shipmentId) {
        return new ShipmentClientResponse(shipmentId, "SHP-20260503-000001", "MANUAL", "TRK123", "CREATED");
    }

    public static CheckoutSubmitResponse submitResponse(UUID checkoutId, UUID orderId, UUID paymentId) {
        return new CheckoutSubmitResponse(
                checkoutId,
                CheckoutStatus.PAYMENT_PENDING,
                orderId,
                "ORD-20260503-000001",
                new CheckoutPaymentActionResponse(paymentId, "session-token", "https://pay.example.com", "IYZICO",
                        "WAITING_PROVIDER_ACTION"),
                new CheckoutMoneyBreakdownResponse(
                        money("2000.00"),
                        BigDecimal.ZERO,
                        money("100.00"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        money("1900.00"),
                        CURRENCY));
    }

    public static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
