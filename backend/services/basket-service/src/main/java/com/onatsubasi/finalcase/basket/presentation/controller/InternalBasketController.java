package com.onatsubasi.finalcase.basket.presentation.controller;

import com.onatsubasi.finalcase.basket.application.dto.internal.MarkBasketCheckedOutRequest;
import com.onatsubasi.finalcase.basket.application.dto.internal.MarkBasketCheckedOutResponse;
import com.onatsubasi.finalcase.basket.application.dto.response.BasketResponse;
import com.onatsubasi.finalcase.basket.application.service.BasketService;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/baskets")
@RequiredArgsConstructor
@Tag(name = "Internal Basket", description = "Internal basket APIs for checkout and lifecycle management")
public class InternalBasketController {

    private final BasketService basketService;

    @GetMapping("/users/{userId}/active")
    @Operation(
            summary = "Get active basket for checkout",
            description = "Internal endpoint used by Checkout Service to read the active basket snapshot."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active basket returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Active basket not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Basket is empty")
    public ResponseEntity<ApiResponse<BasketResponse>> getActiveBasketByUserId(
            @Parameter(description = "User id of the basket owner") @PathVariable java.util.UUID userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(basketService.getBasket(userId))
        );
    }

    @PostMapping("/{basketId}/mark-checked-out")
    @Operation(
            summary = "Mark active basket as checked out",
            description = "Internal endpoint used by Checkout Service after successful payment finalization."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Basket marked as checked out")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Basket not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Basket cannot be checked out")
    public ResponseEntity<ApiResponse<MarkBasketCheckedOutResponse>> markBasketCheckedOut(
            @Parameter(description = "Basket id") @PathVariable java.util.UUID basketId,
            @Valid @RequestBody MarkBasketCheckedOutRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Basket marked checked out",
                        basketService.markBasketCheckedOut(basketId, request)
                )
        );
    }
}