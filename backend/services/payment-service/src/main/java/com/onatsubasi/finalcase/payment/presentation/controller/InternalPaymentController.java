package com.onatsubasi.finalcase.payment.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.payment.application.dto.request.InitializePaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentDetailResponse;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentInitializeResponse;
import com.onatsubasi.finalcase.payment.application.service.PaymentCommandService;
import com.onatsubasi.finalcase.payment.application.service.PaymentQueryService;
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
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
@Tag(name = "Internal Payment", description = "Internal payment APIs used by Checkout Service and other backend services")
public class InternalPaymentController {

        private final PaymentCommandService paymentCommandService;
        private final PaymentQueryService paymentQueryService;

        @Operation(summary = "Initialize payment", description = "Initializes a payment attempt for a checkout/order. Requires Idempotency-Key.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Payment initialized")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payment initialize request")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Payment idempotency conflict")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Payment provider initialization failed")
        @PostMapping("/initialize")
        public ResponseEntity<ApiResponse<PaymentInitializeResponse>> initializePayment(
                        @Parameter(description = "Required idempotency key. Same key and same payload returns stored response.", required = true) @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,

                        @Valid @RequestBody InitializePaymentRequest request) {
                PaymentInitializeResponse response = paymentCommandService.initializePayment(
                                request,
                                idempotencyKey);

                return ResponseEntity
                                .status(HttpStatus.ACCEPTED)
                                .body(ApiResponse.success(response));
        }

        @Operation(summary = "Get payment by order id", description = "Returns payment detail for an order id. Internal use only.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment found")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
        @GetMapping("/orders/{orderId}")
        public ResponseEntity<ApiResponse<PaymentDetailResponse>> getByOrderId(
                        @Parameter(description = "Order id", required = true) @PathVariable UUID orderId) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentQueryService.getByOrderIdForInternal(orderId)));
        }

        @Operation(summary = "Get payment by checkout id", description = "Returns payment detail for a checkout id. Internal use only.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment found")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
        @GetMapping("/checkouts/{checkoutId}")
        public ResponseEntity<ApiResponse<PaymentDetailResponse>> getByCheckoutId(
                        @Parameter(description = "Checkout id", required = true) @PathVariable UUID checkoutId) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentQueryService.getByCheckoutIdForInternal(checkoutId)));
        }
}