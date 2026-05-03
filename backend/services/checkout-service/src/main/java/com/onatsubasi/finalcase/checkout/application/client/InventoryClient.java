package com.onatsubasi.finalcase.checkout.application.client;

import com.onatsubasi.finalcase.checkout.application.dto.client.InventoryReservationClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.client.InventoryReserveClientRequest;
import com.onatsubasi.finalcase.checkout.infrastructure.config.FeignConfig;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(
        name = "inventory-service",
        path = "/internal/inventory",
        configuration = FeignConfig.class
)
public interface InventoryClient {

    @PostMapping("/reservations")
    ApiResponse<InventoryReservationClientResponse> reserveStock(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody InventoryReserveClientRequest request
    );

    @PostMapping("/reservations/{reservationId}/confirm")
    ApiResponse<InventoryReservationClientResponse> confirmReservation(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID reservationId
    );

    @PostMapping("/reservations/{reservationId}/release")
    ApiResponse<InventoryReservationClientResponse> releaseReservation(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID reservationId
    );
}
