package com.onatsubasi.finalcase.shipment.application.client;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.shipment.application.dto.client.MarkOrderDeliveredClientRequest;
import com.onatsubasi.finalcase.shipment.application.dto.client.MarkOrderShippedClientRequest;
import com.onatsubasi.finalcase.shipment.application.dto.client.OrderDetailClientResponse;
import com.onatsubasi.finalcase.shipment.application.dto.client.ShipmentCreatedOrderClientRequest;
import com.onatsubasi.finalcase.shipment.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(
        name = "order-service",
        path = "/internal/orders",
        configuration = FeignConfig.class
)
public interface OrderClient {

    @GetMapping("/{orderId}")
    ApiResponse<OrderDetailClientResponse> getById(
            @PathVariable UUID orderId
    );

    @PostMapping("/{orderId}/shipment-created")
    ApiResponse<OrderDetailClientResponse> updateShipmentCreated(
            @PathVariable UUID orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody ShipmentCreatedOrderClientRequest request
    );

    @PostMapping("/{orderId}/mark-shipped")
    ApiResponse<OrderDetailClientResponse> markShipped(
            @PathVariable UUID orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody MarkOrderShippedClientRequest request
    );

    @PostMapping("/{orderId}/mark-delivered")
    ApiResponse<OrderDetailClientResponse> markDelivered(
            @PathVariable UUID orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody MarkOrderDeliveredClientRequest request
    );
}
