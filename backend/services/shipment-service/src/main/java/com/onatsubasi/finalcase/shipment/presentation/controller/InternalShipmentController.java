package com.onatsubasi.finalcase.shipment.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.shipment.application.dto.request.CreateShipmentForOrderRequest;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentDetailResponse;
import com.onatsubasi.finalcase.shipment.application.service.ShipmentCommandService;
import com.onatsubasi.finalcase.shipment.application.service.ShipmentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/shipments")
@RequiredArgsConstructor
@Tag(
        name = "Internal Shipment",
        description = "Internal shipment APIs used by Checkout Service and other backend services"
)
public class InternalShipmentController {

    private final ShipmentCommandService shipmentCommandService;
    private final ShipmentQueryService shipmentQueryService;

    @Operation(
            summary = "Create shipment for order",
            description = "Creates a shipment from an order snapshot. Requires Idempotency-Key.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Shipment created")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid shipment request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Shipment already exists or idempotency conflict")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Order service unavailable")
    @PostMapping({"", "/orders"})
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> createShipmentForOrder(
            @Parameter(description = "Required idempotency key", required = true)
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,

            @Valid @RequestBody CreateShipmentForOrderRequest request
    ) {
        ShipmentDetailResponse response = shipmentCommandService.createShipmentForOrder(
                idempotencyKey,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(
            summary = "Get shipment by order id",
            description = "Returns shipment detail for an order id. Internal use only.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shipment found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shipment not found")
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> getByOrderId(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shipmentQueryService.getByOrderIdForInternal(orderId)
                )
        );
    }
}