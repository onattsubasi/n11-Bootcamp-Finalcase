package com.onatsubasi.finalcase.checkout.presentation.controller;

import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSessionResponse;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutQueryService;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutRetryService;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/checkouts")
@Tag(name = "Admin Checkouts", description = "Admin checkout inspection and retry operations")
public class AdminCheckoutController {

    private final CheckoutQueryService checkoutQueryService;
    private final CheckoutRetryService checkoutRetryService;

    @Operation(
            summary = "List checkout sessions",
            description = "Lists checkout sessions by optional status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CheckoutSessionResponse>>> listCheckouts(
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<CheckoutSessionResponse> response = checkoutQueryService.listAdminCheckouts(
                status,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @Operation(
            summary = "Get checkout detail",
            description = "Returns full checkout session detail including saga steps.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{checkoutId}")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> getCheckout(
            @PathVariable UUID checkoutId
    ) {
        CheckoutSessionResponse response = checkoutQueryService.getAdminCheckout(checkoutId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @Operation(
            summary = "Retry checkout finalization",
            description = """
                    Retries finalization for PAYMENT_SUCCEEDED or FINALIZATION_FAILED checkouts.
                    This is intended for admin recovery after downstream failures.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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
            summary = "Retry checkout compensation",
            description = """
                    Retries compensation for PAYMENT_FAILED, COMPENSATION_PENDING, or COMPENSATION_FAILED checkouts.
                    This is intended for admin recovery after downstream failures.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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