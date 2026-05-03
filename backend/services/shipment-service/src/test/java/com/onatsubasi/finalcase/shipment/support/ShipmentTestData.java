package com.onatsubasi.finalcase.shipment.support;

import com.onatsubasi.finalcase.shipment.application.dto.client.OrderAddressClientResponse;
import com.onatsubasi.finalcase.shipment.application.dto.client.OrderDetailClientResponse;
import com.onatsubasi.finalcase.shipment.application.dto.client.OrderItemClientResponse;
import com.onatsubasi.finalcase.shipment.application.dto.provider.CarrierCreateShipmentResult;
import com.onatsubasi.finalcase.shipment.application.dto.request.ChangeShipmentStatusRequest;
import com.onatsubasi.finalcase.shipment.application.dto.request.CreateShipmentForOrderRequest;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentAddressSnapshot;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentIdempotencyRecord;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ShipmentTestData {

    public static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final String SHIPMENT_NUMBER = "SHP-20260503-000001";

    private ShipmentTestData() {
    }

    public static ShipmentAddressSnapshot addressSnapshot() {
        return new ShipmentAddressSnapshot(
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

    public static Shipment shipment() {
        Shipment shipment = new Shipment(
                SHIPMENT_NUMBER,
                ORDER_ID,
                "ORD-20260503-000001",
                USER_ID,
                ShipmentCarrier.MANUAL,
                addressSnapshot()
        );
        shipment.addItem(new ShipmentItem("product-1", "SKU-1", "Demo Product", 2));
        return shipment;
    }

    public static CreateShipmentForOrderRequest createRequest() {
        return new CreateShipmentForOrderRequest(ORDER_ID, ShipmentCarrier.MANUAL);
    }

    public static ChangeShipmentStatusRequest statusRequest(ShipmentStatus status) {
        return new ChangeShipmentStatusRequest(
                status,
                status == ShipmentStatus.SHIPPED ? "TRK-123" : null,
                status == ShipmentStatus.SHIPPED ? "https://carrier.example/TRK-123" : null,
                status == ShipmentStatus.DELIVERY_FAILED ? "Address not found" : null,
                "status change"
        );
    }

    public static ShipmentIdempotencyRecord idempotencyRecord(String requestHash) {
        return new ShipmentIdempotencyRecord(
                "checkout:abc:shipment-create",
                requestHash,
                Instant.now().plusSeconds(300)
        );
    }

    public static CarrierCreateShipmentResult successfulCarrierResult() {
        return CarrierCreateShipmentResult.builder()
                .success(true)
                .carrierShipmentId("CARRIER-1")
                .trackingNumber("TRK-1")
                .trackingUrl("https://carrier.example/TRK-1")
                .labelUrl("https://carrier.example/label/TRK-1.pdf")
                .carrierStatus("CREATED")
                .build();
    }

    public static OrderDetailClientResponse paidOrder() {
        return orderWithStatus("PAID");
    }

    public static OrderDetailClientResponse orderWithStatus(String status) {
        return new OrderDetailClientResponse(
                ORDER_ID,
                "ORD-20260503-000001",
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                USER_ID,
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                null,
                status,
                shippingAddress(),
                shippingAddress(),
                BigDecimal.valueOf(200),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(200),
                "TRY",
                List.of(orderItem()),
                Instant.now(),
                Instant.now()
        );
    }

    private static OrderAddressClientResponse shippingAddress() {
        return new OrderAddressClientResponse(
                "SHIPPING",
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

    private static OrderItemClientResponse orderItem() {
        return new OrderItemClientResponse(
                UUID.fromString("66666666-6666-6666-6666-666666666666"),
                "product-1",
                "SKU-1",
                "Demo Product",
                "demo-product",
                "https://cdn.example/product.jpg",
                "brand-1",
                "Demo Brand",
                "category-1",
                "Demo Category",
                BigDecimal.valueOf(100),
                2,
                BigDecimal.valueOf(200),
                BigDecimal.ZERO,
                BigDecimal.valueOf(200),
                "TRY"
        );
    }
}
