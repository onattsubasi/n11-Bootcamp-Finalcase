package com.onatsubasi.finalcase.shipment.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.shipment.application.dto.request.CancelShipmentRequest;
import com.onatsubasi.finalcase.shipment.application.dto.request.ChangeShipmentStatusRequest;
import com.onatsubasi.finalcase.shipment.application.dto.request.UpdateShipmentTrackingRequest;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentDetailResponse;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentSummaryResponse;
import com.onatsubasi.finalcase.shipment.application.service.ShipmentCommandService;
import com.onatsubasi.finalcase.shipment.application.service.ShipmentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/shipments")
@RequiredArgsConstructor
@Tag(
        name = "Shipment Admin",
        description = "Admin shipment inspection, tracking, status update and cancellation APIs"
)
public class AdminShipmentController {

    private final ShipmentQueryService shipmentQueryService;
    private final ShipmentCommandService shipmentCommandService;

    @Operation(
            summary = "List shipments",
            description = "Returns paginated shipment summaries for admin monitoring.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shipments listed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShipmentSummaryResponse>>> getAllShipments(
            @Parameter(description = "Page index", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shipmentQueryService.getAllShipments(page, size)
                )
        );
    }

    @Operation(
            summary = "Get shipment by id",
            description = "Returns shipment detail for admin inspection.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shipment found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shipment not found")
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> getById(
            @Parameter(description = "Shipment id", required = true)
            @PathVariable UUID shipmentId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shipmentQueryService.getByIdForAdmin(shipmentId)
                )
        );
    }

    @Operation(
            summary = "Update shipment tracking",
            description = "Updates tracking number and tracking URL without changing shipment status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tracking updated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shipment not found")
    @PatchMapping("/{shipmentId}/tracking")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> updateTracking(
            @CurrentUser UserContext admin,

            @Parameter(description = "Shipment id", required = true)
            @PathVariable UUID shipmentId,

            @Valid @RequestBody UpdateShipmentTrackingRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shipmentCommandService.updateTracking(
                                shipmentId,
                                admin.userId().toString(),
                                request
                        )
                )
        );
    }

    @Operation(
            summary = "Change shipment status",
            description = "Changes shipment status according to valid shipment lifecycle transitions.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shipment status changed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid shipment status request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shipment not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Invalid shipment status transition")
    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> changeStatus(
            @CurrentUser UserContext admin,

            @Parameter(description = "Shipment id", required = true)
            @PathVariable UUID shipmentId,

            @Valid @RequestBody ChangeShipmentStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shipmentCommandService.changeStatus(
                                shipmentId,
                                admin.userId().toString(),
                                request
                        )
                )
        );
    }

    @Operation(
            summary = "Cancel shipment",
            description = "Cancels shipment if current shipment status allows cancellation.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shipment cancelled")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shipment not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Shipment cannot be cancelled")
    @PostMapping("/{shipmentId}/cancel")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> cancelShipment(
            @CurrentUser UserContext admin,

            @Parameter(description = "Shipment id", required = true)
            @PathVariable UUID shipmentId,

            @Valid @RequestBody(required = false) CancelShipmentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        shipmentCommandService.cancelShipment(
                                shipmentId,
                                admin.userId().toString(),
                                request
                        )
                )
        );
    }
}