package com.onatsubasi.finalcase.shipment.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentDetailResponse;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentSummaryResponse;
import com.onatsubasi.finalcase.shipment.application.service.ShipmentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer/shipments")
@RequiredArgsConstructor
@Tag(
        name = "Shipment",
        description = "Customer shipment query APIs"
)
public class CustomerShipmentController {

    private final ShipmentQueryService shipmentQueryService;

    @Operation(
            summary = "List my shipments",
            description = "Returns paginated shipment summaries for the authenticated customer.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shipments listed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShipmentSummaryResponse>>> getMyShipments(
            @CurrentUser UserContext user,

            @Parameter(description = "Page index", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shipmentQueryService.getMyShipments(
                                user.userId(),
                                page,
                                size
                        )
                )
        );
    }

    @Operation(
            summary = "Get my shipment by id",
            description = "Returns shipment detail if it belongs to the authenticated customer.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shipment found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Shipment access denied")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shipment not found")
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> getById(
            @CurrentUser UserContext user,

            @Parameter(description = "Shipment id", required = true)
            @PathVariable UUID shipmentId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shipmentQueryService.getByIdForCustomer(
                                shipmentId,
                                user.userId()
                        )
                )
        );
    }
}