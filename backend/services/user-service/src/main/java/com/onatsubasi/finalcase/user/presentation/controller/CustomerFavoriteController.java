package com.onatsubasi.finalcase.user.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.AddFavoriteProductRequest;
import com.onatsubasi.finalcase.user.application.dto.response.FavoriteProductResponse;
import com.onatsubasi.finalcase.user.application.service.FavoriteProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/favorites")
@Tag(name = "Customer Favorites", description = "Current customer favorite product operations")
public class CustomerFavoriteController {

    private final FavoriteProductService favoriteProductService;

    @Operation(
            summary = "List my favorites",
            description = "Lists product ids favorited by current customer.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteProductResponse>>> listMyFavorites(
            @CurrentUser UserContext userContext
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(favoriteProductService.listMyFavorites(userContext))
        );
    }

    @Operation(
            summary = "Add favorite product",
            description = "Adds a product id to current customer's favorites. Duplicate add is idempotent.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteProductResponse>> addFavorite(
            @CurrentUser UserContext userContext,
            @Valid @RequestBody AddFavoriteProductRequest request
    ) {
        FavoriteProductResponse response = favoriteProductService.addFavorite(
                userContext,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Favorite product added successfully", response));
    }

    @Operation(
            summary = "Remove favorite product",
            description = "Removes a product id from current customer's favorites.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @CurrentUser UserContext userContext,
            @PathVariable UUID productId
    ) {
        favoriteProductService.removeFavorite(userContext, productId);

        return ResponseEntity.ok(
                ApiResponse.success("Favorite product removed successfully")
        );
    }
}
