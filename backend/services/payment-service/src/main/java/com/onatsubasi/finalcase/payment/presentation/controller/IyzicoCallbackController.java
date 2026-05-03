package com.onatsubasi.finalcase.payment.presentation.controller;

import com.onatsubasi.finalcase.payment.application.dto.request.IyzicoCheckoutFormCallbackRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentDetailResponse;
import com.onatsubasi.finalcase.payment.application.service.PaymentCallbackService;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Iyzico Callback", description = "Iyzico provider callback endpoints")
public class IyzicoCallbackController {

        private final PaymentCallbackService paymentCallbackService;

        @Operation(summary = "Handle Iyzico Checkout Form callback", description = "Receives Iyzico Checkout Form callback token, retrieves provider payment result, updates payment state, and publishes payment result event.")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Callback processed")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid callback payload")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment attempt not found")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Provider retrieve failed")
        @PostMapping({"/api/payments/providers/iyzico/checkout-form/callback", "/api/payments/iyzico/callback"})
        public ResponseEntity<ApiResponse<PaymentDetailResponse>> handleCheckoutFormCallback(
                        @Valid @RequestBody IyzicoCheckoutFormCallbackRequest request) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                paymentCallbackService.handleIyzicoCheckoutFormCallback(request)));
        }
}