package com.onatsubasi.finalcase.checkout.presentation.controller;

import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutQuoteRequest;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutSubmitRequest;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutQuoteResponse;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSessionResponse;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSubmitResponse;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutQueryService;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutQuoteService;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutSubmitService;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/checkout")
@Tag(name = "Customer Checkout", description = "Customer checkout quote, submit, and query operations")
public class CustomerCheckoutController {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final CheckoutQuoteService checkoutQuoteService;
    private final CheckoutSubmitService checkoutSubmitService;
    private final CheckoutQueryService checkoutQueryService;

    @Operation(
            summary = "Calculate checkout quote",
            description = """
                    Calculates a read-only checkout quote from basket, catalog, user address, and promotion data.
                    This endpoint does not reserve stock, reserve promotion usage, create order, or initialize payment.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/quote")
    public ResponseEntity<ApiResponse<CheckoutQuoteResponse>> quote(
            @CurrentUser UserContext currentUser,
            @Valid @RequestBody CheckoutQuoteRequest request
    ) {
        CheckoutQuoteResponse response = checkoutQuoteService.quote(currentUser, request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @Operation(
            summary = "Submit checkout",
            description = """
                    Starts the checkout saga.
                    Requires Idempotency-Key header.
                    Reserves stock, creates order, reserves promotion usage if needed, and initializes payment.
                    Successful response means payment is pending, not that the order is paid.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<CheckoutSubmitResponse>> submit(
            @CurrentUser UserContext currentUser,
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CheckoutSubmitRequest request
    ) {
        CheckoutSubmitResponse response = checkoutSubmitService.submit(
                currentUser,
                request,
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Checkout submitted successfully", response));
    }

    @Operation(
            summary = "Get my checkout",
            description = "Returns current customer's checkout session detail.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{checkoutId}")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> getMyCheckout(
            @CurrentUser UserContext currentUser,
            @PathVariable UUID checkoutId
    ) {
        CheckoutSessionResponse response = checkoutQueryService.getMyCheckout(
                currentUser,
                checkoutId
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @Operation(
            summary = "List my checkouts",
            description = "Lists current customer's checkout sessions.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<CheckoutSessionResponse>>> listMyCheckouts(
            @CurrentUser UserContext currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<CheckoutSessionResponse> response = checkoutQueryService.listMyCheckouts(
                currentUser,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}