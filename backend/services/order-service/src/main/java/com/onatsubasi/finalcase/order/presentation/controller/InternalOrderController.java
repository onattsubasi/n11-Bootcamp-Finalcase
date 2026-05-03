package com.onatsubasi.finalcase.order.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.order.application.dto.internal.CreateOrderInternalRequest;
import com.onatsubasi.finalcase.order.application.dto.internal.MarkOrderDeliveredRequest;
import com.onatsubasi.finalcase.order.application.dto.internal.MarkOrderPaidRequest;
import com.onatsubasi.finalcase.order.application.dto.internal.MarkOrderPaymentFailedRequest;
import com.onatsubasi.finalcase.order.application.dto.internal.MarkOrderShippedRequest;
import com.onatsubasi.finalcase.order.application.dto.internal.OrderReviewEligibilityResponse;
import com.onatsubasi.finalcase.order.application.dto.internal.ShipmentCreatedRequest;
import com.onatsubasi.finalcase.order.application.dto.request.CancelOrderRequest;
import com.onatsubasi.finalcase.order.application.dto.response.OrderDetailResponse;
import com.onatsubasi.finalcase.order.application.service.OrderCommandService;
import com.onatsubasi.finalcase.order.application.service.OrderQueryService;
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
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
@Tag(
        name = "Internal Order",
        description = "Internal order APIs used by Checkout, Shipment and Review services"
)
public class InternalOrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    @Operation(
            summary = "Create order from checkout",
            description = "Creates an order from Checkout Service snapshot. This endpoint is internal only.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid order request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Order already exists or idempotency conflict")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailResponse>> createOrder(
            @Valid @RequestBody CreateOrderInternalRequest request
    ) {
        OrderDetailResponse response = orderCommandService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(
            summary = "Get order by id",
            description = "Returns order detail by order id for internal services.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getById(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderQueryService.getByIdForInternal(orderId)
                )
        );
    }

    @Operation(
            summary = "Get order by checkout id",
            description = "Returns order detail by checkout id for Checkout Service idempotency/recovery.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/checkout/{checkoutId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getByCheckoutId(
            @Parameter(description = "Checkout id", required = true)
            @PathVariable UUID checkoutId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderQueryService.getByCheckoutIdForInternal(checkoutId)
                )
        );
    }

    @Operation(
            summary = "Mark order as paid",
            description = "Marks a pending order as paid after successful payment finalization.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order marked as paid")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Invalid order status transition")
    @PostMapping("/{orderId}/mark-paid")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> markPaid(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId,

            @Valid @RequestBody MarkOrderPaidRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderCommandService.markPaid(orderId, request)
                )
        );
    }

    @Operation(
            summary = "Mark order payment as failed",
            description = "Marks a pending order as payment failed after unsuccessful payment finalization.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order marked as payment failed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Invalid order status transition")
    @PostMapping("/{orderId}/mark-payment-failed")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> markPaymentFailed(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId,

            @Valid @RequestBody MarkOrderPaymentFailedRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderCommandService.markPaymentFailed(orderId, request)
                )
        );
    }

    @Operation(
            summary = "Cancel order internally",
            description = "Cancels an order from Checkout compensation or internal operational flows.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order cancelled")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Order cannot be cancelled")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId,

            @Valid @RequestBody(required = false) CancelOrderRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderCommandService.cancelInternal(orderId, request)
                )
        );
    }

    @Operation(
            summary = "Attach shipment summary",
            description = "Stores shipment summary after Shipment Service creates shipment for the order.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shipment summary attached")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Invalid order status transition")
    @PostMapping("/{orderId}/shipment-created")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateShipmentCreated(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId,

            @Valid @RequestBody ShipmentCreatedRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderCommandService.attachShipmentCreated(orderId, request)
                )
        );
    }

    @Operation(
            summary = "Mark order as shipped",
            description = "Updates order shipment summary and moves order to shipped state.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order marked as shipped")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Invalid order status transition")
    @PostMapping("/{orderId}/mark-shipped")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> markShipped(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId,

            @Valid @RequestBody MarkOrderShippedRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderCommandService.markShipped(orderId, request)
                )
        );
    }

    @Operation(
            summary = "Mark order as delivered",
            description = "Moves order to delivered state. Review Service uses delivered orders for verified purchase checks.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order marked as delivered")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Invalid order status transition")
    @PostMapping("/{orderId}/mark-delivered")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> markDelivered(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId,

            @Valid @RequestBody MarkOrderDeliveredRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderCommandService.markDelivered(orderId, request)
                )
        );
    }

    @Operation(
            summary = "Verify review eligibility",
            description = "Checks whether the user owns a delivered order item for the requested product.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review eligibility returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{orderId}/items/{orderItemId}/review-eligibility")
    public ResponseEntity<ApiResponse<OrderReviewEligibilityResponse>> verifyReviewEligibility(
            @Parameter(description = "Order id", required = true)
            @PathVariable UUID orderId,

            @Parameter(description = "Order item id", required = true)
            @PathVariable UUID orderItemId,

            @Parameter(description = "User id", required = true)
            @RequestParam UUID userId,

            @Parameter(description = "Product id", required = true)
            @RequestParam String productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderQueryService.verifyReviewEligibility(
                                orderId,
                                orderItemId,
                                userId,
                                productId
                        )
                )
        );
    }
}