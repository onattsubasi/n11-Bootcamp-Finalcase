package com.onatsubasi.finalcase.payment.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.payment.application.dto.request.CancelPaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.request.RefundPaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentCancellationResponse;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentDetailResponse;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentRefundResponse;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentSummaryResponse;
import com.onatsubasi.finalcase.payment.application.service.PaymentQueryService;
import com.onatsubasi.finalcase.payment.application.service.PaymentRefundService;
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
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Admin", description = "Admin payment inspection, refund, and cancellation APIs")
public class AdminPaymentController {

        private final PaymentQueryService paymentQueryService;
        private final PaymentRefundService paymentRefundService;

        @Operation(summary = "List payments", description = "Returns paginated payment summaries for admin monitoring.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments listed")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
        @GetMapping
        public ResponseEntity<ApiResponse<Page<PaymentSummaryResponse>>> getAllPayments(
                        @Parameter(description = "Page index", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentQueryService.getAllPayments(page, size)));
        }

        @Operation(summary = "Get payment by id", description = "Returns payment detail for admin inspection.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment found")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
        @GetMapping("/{paymentId}")
        public ResponseEntity<ApiResponse<PaymentDetailResponse>> getById(
                        @Parameter(description = "Payment id", required = true) @PathVariable UUID paymentId) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentQueryService.getByIdForAdmin(paymentId)));
        }

        @Operation(summary = "List payment refunds", description = "Returns refund records for a payment.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Refunds listed")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
        @GetMapping("/{paymentId}/refunds")
        public ResponseEntity<ApiResponse<Page<PaymentRefundResponse>>> getRefunds(
                        @Parameter(description = "Payment id", required = true) @PathVariable UUID paymentId,

                        @Parameter(description = "Page index", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentQueryService.getRefunds(paymentId, page, size)));
        }

        @Operation(summary = "Refund payment", description = "Creates a full or partial refund. Requires Idempotency-Key.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Refund processed")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid refund request")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Refund not allowed")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Provider refund failed")
        @PostMapping("/{paymentId}/refunds")
        public ResponseEntity<ApiResponse<PaymentRefundResponse>> refundPayment(
                        @Parameter(description = "Payment id", required = true) @PathVariable UUID paymentId,

                        @Parameter(description = "Required idempotency key", required = true) @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,

                        @Valid @RequestBody RefundPaymentRequest request) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentRefundService.refundPayment(
                                                                paymentId,
                                                                idempotencyKey,
                                                                request)));
        }

        @Operation(summary = "Cancel payment", description = "Cancels a payment before settlement if provider and payment status allow it. Requires Idempotency-Key.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cancellation processed")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid cancellation request")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cancellation not allowed")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Provider cancellation failed")
        @PostMapping("/{paymentId}/cancel")
        public ResponseEntity<ApiResponse<PaymentCancellationResponse>> cancelPayment(
                        @Parameter(description = "Payment id", required = true) @PathVariable UUID paymentId,

                        @Parameter(description = "Required idempotency key", required = true) @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,

                        @Valid @RequestBody(required = false) CancelPaymentRequest request) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentRefundService.cancelPayment(
                                                                paymentId,
                                                                idempotencyKey,
                                                                request)));
        }
}