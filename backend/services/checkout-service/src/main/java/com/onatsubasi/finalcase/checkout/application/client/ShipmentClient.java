package com.onatsubasi.finalcase.checkout.application.client;

import com.onatsubasi.finalcase.checkout.application.dto.client.CreateShipmentForOrderClientRequest;
import com.onatsubasi.finalcase.checkout.application.dto.client.ShipmentClientResponse;
import com.onatsubasi.finalcase.checkout.infrastructure.config.FeignConfig;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "shipment-service",
        path = "/internal/shipments",
        configuration = FeignConfig.class
)
public interface ShipmentClient {

    @PostMapping("/orders")
    ApiResponse<ShipmentClientResponse> createShipmentForOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreateShipmentForOrderClientRequest request
    );
}
