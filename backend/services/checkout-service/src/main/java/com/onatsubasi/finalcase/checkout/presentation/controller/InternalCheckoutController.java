package com.onatsubasi.finalcase.checkout.presentation.controller;

import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSessionResponse;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutQueryService;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutRetryService;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/checkouts")
@Tag(name = "Internal Checkouts", description = "Internal checkout lookup and recovery endpoints")
public class InternalCheckoutController {

    private final CheckoutQueryService checkoutQueryService;
    private final CheckoutRetryService checkoutRetryService;

    @Operation(
            summary = "Get internal checkout detail",
            description = "Internal checkout lookup by checkout id."
    )
    @GetMapping("/{checkoutId}")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> getCheckout(
            @PathVariable UUID checkoutId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(checkoutQueryService.getAdminCheckout(checkoutId))
        );
    }

    @Operation(
            summary = "Retry internal checkout finalization",
            description = "Internal recovery endpoint for retrying checkout finalization."
    )
    @PostMapping("/{checkoutId}/retry-finalization")
    public ResponseEntity<ApiResponse<Void>> retryFinalization(
            @PathVariable UUID checkoutId
    ) {
        checkoutRetryService.retryFinalization(checkoutId);

        return ResponseEntity.ok(
                ApiResponse.success("Checkout finalization retry triggered successfully")
        );
    }

    @Operation(
            summary = "Retry internal checkout compensation",
            description = "Internal recovery endpoint for retrying checkout compensation."
    )
    @PostMapping("/{checkoutId}/retry-compensation")
    public ResponseEntity<ApiResponse<Void>> retryCompensation(
            @PathVariable UUID checkoutId
    ) {
        checkoutRetryService.retryCompensation(checkoutId);

        return ResponseEntity.ok(
                ApiResponse.success("Checkout compensation retry triggered successfully")
        );
    }
}