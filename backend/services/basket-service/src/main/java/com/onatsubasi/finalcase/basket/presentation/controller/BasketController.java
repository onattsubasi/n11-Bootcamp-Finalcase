package com.onatsubasi.finalcase.basket.presentation.controller;

import com.onatsubasi.finalcase.basket.application.dto.request.AddBasketItemRequest;
import com.onatsubasi.finalcase.basket.application.dto.request.UpdateBasketItemQuantityRequest;
import com.onatsubasi.finalcase.basket.application.dto.request.UpdateCouponIntentRequest;
import com.onatsubasi.finalcase.basket.application.dto.response.BasketResponse;
import com.onatsubasi.finalcase.basket.application.service.BasketService;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer/basket")
@RequiredArgsConstructor
@Tag(name = "Basket", description = "Customer basket management: add items, update quantity, clear basket")
public class BasketController {

    private final BasketService basketService;

    @GetMapping
    @Operation(
            summary = "Get active basket",
            description = "Returns the current customer's active basket. Creates an empty active basket if none exists.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active basket returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<ApiResponse<BasketResponse>> getBasket(
            @CurrentUser UserContext userContext
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(basketService.getBasket(userContext))
        );
    }

    @PostMapping("/items")
    @Operation(
            summary = "Add item to basket",
            description = "Adds a product reference and quantity to the active basket. Basket does not reserve stock or trust product price.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item added successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<ApiResponse<BasketResponse>> addItem(
            @CurrentUser UserContext userContext,
            @Valid @RequestBody AddBasketItemRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Item added to basket",
                        basketService.addItem(userContext, request)
                )
        );
    }

    @PutMapping("/items/{productId}")
    @Operation(
            summary = "Update basket item quantity",
            description = "Updates quantity for a product already present in the active basket.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Basket item updated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<ApiResponse<BasketResponse>> updateItemQuantity(
            @CurrentUser UserContext userContext,
            @Parameter(description = "Product id") @PathVariable UUID productId,
            @Valid @RequestBody UpdateBasketItemQuantityRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Basket item updated",
                        basketService.updateItemQuantity(userContext, productId, request)
                )
        );
    }

    @DeleteMapping("/items/{productId}")
    @Operation(
            summary = "Remove item from basket",
            description = "Removes product from active basket if present. Operation is idempotent.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Item removed from basket")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<Void> removeItem(
            @CurrentUser UserContext userContext,
            @Parameter(description = "Product id") @PathVariable UUID productId
    ) {
        basketService.removeItem(userContext, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(
            summary = "Clear active basket",
            description = "Removes all items from the current customer's active basket and keeps the basket ACTIVE.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Basket cleared")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<Void> clearBasket(
            @CurrentUser UserContext userContext
    ) {
        basketService.clearBasket(userContext);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items")
    @Operation(
            summary = "Clear active basket items",
            description = "Backward-compatible alias for clearing the current customer's basket items.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Basket cleared")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<Void> clearBasketItemsAlias(
            @CurrentUser UserContext userContext
    ) {
        basketService.clearBasket(userContext);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/coupon-intent")
    @Operation(
            summary = "Update coupon code intent",
            description = "Stores a customer-entered coupon code as basket intent only. Promotion Service validates and prices it later.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coupon intent updated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<ApiResponse<BasketResponse>> updateCouponIntent(
            @CurrentUser UserContext userContext,
            @Valid @RequestBody UpdateCouponIntentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Coupon intent updated",
                        basketService.updateCouponIntent(userContext, request)
                )
        );
    }

    @DeleteMapping("/coupon-intent")
    @Operation(
            summary = "Clear coupon code intent",
            description = "Clears customer-entered coupon code intent from the active basket.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Coupon intent cleared")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<Void> clearCouponIntent(
            @CurrentUser UserContext userContext
    ) {
        basketService.clearCouponIntent(userContext);
        return ResponseEntity.noContent().build();
    }
}
