package com.onatsubasi.finalcase.order.support;

import com.onatsubasi.finalcase.order.application.dto.internal.*;
import com.onatsubasi.finalcase.order.application.dto.response.OrderDetailResponse;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatusChangeSource;
import com.onatsubasi.finalcase.order.domain.entity.Order;
import com.onatsubasi.finalcase.order.domain.entity.OrderAddressSnapshot;
import com.onatsubasi.finalcase.order.domain.entity.OrderDiscount;
import com.onatsubasi.finalcase.order.domain.entity.OrderItem;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderTestData {

    public static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID ORDER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID CHECKOUT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID BASKET_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID ORDER_ITEM_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    public static final UUID PAYMENT_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    public static final UUID SHIPMENT_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    private OrderTestData() {
    }

    public static OrderAddressSnapshot address() {
        return new OrderAddressSnapshot(
                "Oytun Coban",
                "+905551112233",
                "Türkiye",
                "İstanbul",
                "Kadıköy",
                "Caferağa",
                "Example street no 1",
                "Floor 2",
                "34710"
        );
    }

    public static OrderItem item() {
        return new OrderItem(
                "product-1",
                "SKU-1",
                "Example Phone",
                "example-phone",
                "https://cdn.example.com/p1.jpg",
                "brand-1",
                "Example Brand",
                "category-1",
                "Phones",
                money("100.00"),
                2,
                money("200.00"),
                money("10.00"),
                money("190.00"),
                "TRY"
        );
    }

    public static OrderDiscount discount() {
        return new OrderDiscount(
                UUID.fromString("99999999-9999-9999-9999-999999999999"),
                "Welcome discount",
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "WELCOME10",
                money("10.00"),
                money("0.00")
        );
    }

    public static Order pendingOrder() {
        Order order = new Order(
                "ORD-20260503-000001",
                CHECKOUT_ID,
                "idem-1",
                "hash-1",
                USER_ID,
                BASKET_ID,
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                address(),
                address(),
                money("200.00"),
                money("0.00"),
                money("10.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("190.00"),
                "try"
        );
        order.addItem(item());
        order.addDiscount(discount());
        ReflectionTestUtils.setField(order, "id", ORDER_ID);
        ReflectionTestUtils.setField(order.getItems().get(0), "id", ORDER_ITEM_ID);
        return order;
    }

    public static Order paidOrder() {
        Order order = pendingOrder();
        order.markPaid(PAYMENT_ID, "IYZICO", "SUCCEEDED", "tx-1", OrderStatusChangeSource.PAYMENT_SERVICE);
        return order;
    }

    public static Order deliveredOrder() {
        Order order = paidOrder();
        order.markShipped("MANUAL", "TRK-1", Instant.parse("2026-05-03T10:00:00Z"), OrderStatusChangeSource.SHIPMENT_SERVICE);
        order.markDelivered(Instant.parse("2026-05-04T10:00:00Z"), OrderStatusChangeSource.SHIPMENT_SERVICE);
        return order;
    }

    public static CreateOrderInternalRequest createOrderRequest() {
        return new CreateOrderInternalRequest(
                CHECKOUT_ID,
                "idem-1",
                "hash-1",
                USER_ID,
                BASKET_ID,
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                addressRequest(),
                addressRequest(),
                money("200.00"),
                money("0.00"),
                money("10.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("190.00"),
                "TRY",
                List.of(itemRequest()),
                List.of(discountRequest())
        );
    }

    public static CreateOrderInternalRequest createOrderRequest(String requestHash) {
        CreateOrderInternalRequest base = createOrderRequest();
        return new CreateOrderInternalRequest(
                base.checkoutId(), base.idempotencyKey(), requestHash, base.userId(), base.basketId(),
                base.inventoryReservationId(), base.promotionUsageReservationId(), base.shippingAddress(), base.billingAddress(),
                base.subtotalAmount(), base.itemDiscountAmount(), base.promotionDiscountAmount(), base.shippingFee(),
                base.shippingDiscountAmount(), base.taxAmount(), base.grandTotalAmount(), base.currency(), base.items(), base.discounts()
        );
    }

    public static OrderAddressSnapshotRequest addressRequest() {
        return new OrderAddressSnapshotRequest(
                "Oytun Coban",
                "+905551112233",
                "Türkiye",
                "İstanbul",
                "Kadıköy",
                "Caferağa",
                "Example street no 1",
                "Floor 2",
                "34710"
        );
    }

    public static CreateOrderItemRequest itemRequest() {
        return new CreateOrderItemRequest(
                "product-1",
                "SKU-1",
                "Example Phone",
                "example-phone",
                "https://cdn.example.com/p1.jpg",
                "brand-1",
                "Example Brand",
                "category-1",
                "Phones",
                money("100.00"),
                2,
                money("200.00"),
                money("10.00"),
                money("190.00"),
                "TRY"
        );
    }

    public static CreateOrderDiscountRequest discountRequest() {
        return new CreateOrderDiscountRequest(
                UUID.fromString("99999999-9999-9999-9999-999999999999"),
                "Welcome discount",
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "welcome10",
                money("10.00"),
                money("0.00")
        );
    }

    public static MarkOrderPaidRequest markPaidRequest() {
        return new MarkOrderPaidRequest(PAYMENT_ID, "IYZICO", "SUCCEEDED", "tx-1");
    }

    public static MarkOrderPaymentFailedRequest markPaymentFailedRequest() {
        return new MarkOrderPaymentFailedRequest(PAYMENT_ID, "IYZICO", "FAILED", "tx-1");
    }

    public static ShipmentCreatedRequest shipmentCreatedRequest() {
        return new ShipmentCreatedRequest(SHIPMENT_ID, "SHP-20260503-000001", "MANUAL", "TRK-1", "CREATED");
    }

    public static OrderDetailResponse detailResponse(OrderStatus status) {
        return new OrderDetailResponse(
                ORDER_ID,
                "ORD-20260503-000001",
                CHECKOUT_ID,
                USER_ID,
                BASKET_ID,
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                status,
                null,
                null,
                null,
                null,
                money("200.00"),
                money("0.00"),
                money("10.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("190.00"),
                "TRY",
                List.of(),
                List.of(),
                List.of(),
                Instant.parse("2026-05-03T10:00:00Z"),
                Instant.parse("2026-05-03T10:00:00Z")
        );
    }

    public static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
