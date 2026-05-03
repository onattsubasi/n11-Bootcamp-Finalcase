package com.onatsubasi.finalcase.order.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.order.application.dto.request.CancelOrderRequest;
import com.onatsubasi.finalcase.order.application.dto.response.OrderDetailResponse;
import com.onatsubasi.finalcase.order.application.dto.response.OrderSummaryResponse;
import com.onatsubasi.finalcase.order.application.service.OrderCommandService;
import com.onatsubasi.finalcase.order.application.service.OrderQueryService;
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
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Order Admin", description = "Admin order listing, lookup, and operational status actions")
public class AdminOrderController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    @Operation(summary = "List all orders", description = "Returns paginated order summaries for admin users.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderQueryService.getAllOrders(page, size)));
    }

    @Operation(summary = "Get order by id", description = "Returns a full order detail for admin users.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getById(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderQueryService.getByIdForAdmin(orderId)));
    }

    @Operation(summary = "Get order by order number", description = "Returns a full order detail by human-readable order number.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getByOrderNumber(
            @Parameter(description = "Order number", required = true)
            @PathVariable String orderNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderQueryService.getByOrderNumberForAdmin(orderNumber)));
    }

    @Operation(summary = "Cancel order as admin", description = "Cancels an order when its current status allows cancellation.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancel(
            @CurrentUser UserContext admin,
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderCommandService.cancelByAdmin(orderId, admin.userId().toString(), request)));
    }

    @Operation(summary = "Mark order preparing", description = "Moves a paid order into preparing status.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{orderId}/preparing")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> markPreparing(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderCommandService.markPreparing(orderId)));
    }
}
