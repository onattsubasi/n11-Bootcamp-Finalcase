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
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Customer order history and order detail APIs")
public class CustomerOrderController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    @Operation(summary = "List my orders", description = "Returns the authenticated customer's own order summaries.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getMyOrders(
            @CurrentUser UserContext user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderQueryService.getMyOrders(user.userId(), page, size)));
    }

    @Operation(summary = "Get my order detail", description = "Returns one order detail only if it belongs to the authenticated customer.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getById(
            @CurrentUser UserContext user,
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderQueryService.getByIdForCustomer(orderId, user.userId())));
    }

    @Operation(summary = "Cancel my order", description = "Cancels one of the authenticated customer's orders if cancellation is allowed.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancel(
            @CurrentUser UserContext user,
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderCommandService.cancelByCustomer(orderId, user.userId(), request)));
    }
}
