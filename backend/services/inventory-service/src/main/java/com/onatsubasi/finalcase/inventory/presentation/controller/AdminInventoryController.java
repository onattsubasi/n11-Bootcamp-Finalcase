package com.onatsubasi.finalcase.inventory.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.inventory.application.dto.request.*;
import com.onatsubasi.finalcase.inventory.application.dto.response.InventoryItemResponse;
import com.onatsubasi.finalcase.inventory.application.dto.response.StockMovementResponse;
import com.onatsubasi.finalcase.inventory.application.service.InventoryAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/inventory")
@Tag(name = "Inventory", description = "Admin inventory and stock management")
public class AdminInventoryController {

    private final InventoryAdminService inventoryAdminService;

    @Operation(
            summary = "Create inventory item",
            description = "Creates the inventory record for a Catalog product. One product can have only one inventory item.",
            security = @SecurityRequirement(name = "bearerAuth")
    )

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Inventory item created successfully"
            )
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden"
            )
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Inventory item already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> createInventoryItem(
            @CurrentUser UserContext admin,
            @Valid @RequestBody CreateInventoryItemRequest request
    ) {
        InventoryItemResponse response = inventoryAdminService.createInventoryItem(admin, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory item created successfully", response));
    }

    @Operation(
            summary = "List active inventory items",
            description = "Lists active inventory items with calculated available quantity and stock status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> listActiveItems() {
        return ResponseEntity.ok(
                ApiResponse.success(inventoryAdminService.listActiveItems())
        );
    }

    @Operation(
            summary = "Get inventory item by product id",
            description = "Returns the inventory item for the given product id.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Inventory item returned successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Inventory item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> getByProductId(
            @Parameter(description = "Catalog product id")
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(inventoryAdminService.getByProductId(productId))
        );
    }

    @Operation(
            summary = "Increase stock",
            description = "Increases total stock for the product and records a stock movement audit entry.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/products/{productId}/increase")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> increaseStock(
            @CurrentUser UserContext admin,

            @Parameter(description = "Catalog product id")
            @PathVariable UUID productId,

            @Valid @RequestBody IncreaseStockRequest request
    ) {
        InventoryItemResponse response = inventoryAdminService.increaseStock(admin, productId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Stock increased successfully", response)
        );
    }

    @Operation(
            summary = "Decrease stock",
            description = "Decreases total stock for the product if total quantity does not fall below reserved quantity.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/products/{productId}/decrease")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> decreaseStock(
            @CurrentUser UserContext admin,

            @Parameter(description = "Catalog product id")
            @PathVariable UUID productId,

            @Valid @RequestBody DecreaseStockRequest request
    ) {
        InventoryItemResponse response = inventoryAdminService.decreaseStock(admin, productId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Stock decreased successfully", response)
        );
    }

    @Operation(
            summary = "Set total stock",
            description = "Sets total stock for the product if the new total is not lower than reserved quantity.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/products/{productId}/stock")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> setStock(
            @CurrentUser UserContext admin,

            @Parameter(description = "Catalog product id")
            @PathVariable UUID productId,

            @Valid @RequestBody SetStockRequest request
    ) {
        InventoryItemResponse response = inventoryAdminService.setStock(admin, productId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Stock set successfully", response)
        );
    }

    @Operation(
            summary = "Update low stock threshold",
            description = "Updates the low-stock threshold used to classify LOW_STOCK state.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/products/{productId}/low-stock-threshold")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateLowStockThreshold(
            @Parameter(description = "Catalog product id")
            @PathVariable UUID productId,

            @Valid @RequestBody UpdateLowStockThresholdRequest request
    ) {
        InventoryItemResponse response = inventoryAdminService.updateLowStockThreshold(productId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Low stock threshold updated successfully", response)
        );
    }

    @Operation(
            summary = "Get stock movement audit trail",
            description = "Returns stock movement audit records for the given product.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/products/{productId}/movements")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getMovements(
            @Parameter(description = "Catalog product id")
            @PathVariable UUID productId
    ) {
        List<StockMovementResponse> response = inventoryAdminService.getMovements(productId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}