package com.onatsubasi.finalcase.checkout.application.client;

import com.onatsubasi.finalcase.checkout.application.dto.client.*;
import com.onatsubasi.finalcase.checkout.infrastructure.config.FeignConfig;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "order-service",
        path = "/internal/orders",
        configuration = FeignConfig.class
)
public interface OrderClient {

    @PostMapping
    ApiResponse<OrderClientResponse> createOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreateOrderClientRequest request
    );

    @GetMapping("/checkout/{checkoutId}")
    ApiResponse<OrderClientResponse> getByCheckoutId(
            @PathVariable UUID checkoutId
    );

    @PostMapping("/{orderId}/mark-paid")
    ApiResponse<OrderClientResponse> markPaid(
            @PathVariable UUID orderId,
            @RequestBody MarkOrderPaidClientRequest request
    );

    @PostMapping("/{orderId}/mark-payment-failed")
    ApiResponse<OrderClientResponse> markPaymentFailed(
            @PathVariable UUID orderId,
            @RequestBody MarkOrderPaymentFailedClientRequest request
    );

    @PostMapping("/{orderId}/shipment-created")
    ApiResponse<OrderClientResponse> updateShipmentCreated(
            @PathVariable UUID orderId,
            @RequestBody ShipmentCreatedOrderClientRequest request
    );
}
