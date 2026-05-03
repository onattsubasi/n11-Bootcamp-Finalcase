package com.onatsubasi.finalcase.payment.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentDetailResponse;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentSummaryResponse;
import com.onatsubasi.finalcase.payment.application.service.PaymentQueryService;
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
@RequestMapping("/api/customer/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Customer payment query APIs")
public class CustomerPaymentController {

        private final PaymentQueryService paymentQueryService;

        @Operation(summary = "List my payments", description = "Returns paginated payment summaries for the authenticated customer.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments listed")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        @GetMapping
        public ResponseEntity<ApiResponse<Page<PaymentSummaryResponse>>> getMyPayments(
                        @CurrentUser UserContext user,

                        @Parameter(description = "Page index", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentQueryService.getMyPayments(
                                                                user.userId(),
                                                                page,
                                                                size)));
        }

        @Operation(summary = "Get my payment by id", description = "Returns payment detail if it belongs to the authenticated customer.", security = @SecurityRequirement(name = "bearerAuth"))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment found")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Payment access denied")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
        @GetMapping("/{paymentId}")
        public ResponseEntity<ApiResponse<PaymentDetailResponse>> getById(
                        @CurrentUser UserContext user,

                        @Parameter(description = "Payment id", required = true) @PathVariable UUID paymentId) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentQueryService.getByIdForCustomer(
                                                                paymentId,
                                                                user.userId())));
        }
}