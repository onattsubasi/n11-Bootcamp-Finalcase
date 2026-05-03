package com.onatsubasi.finalcase.user.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.AddProductListItemRequest;
import com.onatsubasi.finalcase.user.application.dto.request.CreateProductListRequest;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateProductListRequest;
import com.onatsubasi.finalcase.user.application.dto.response.ProductListResponse;
import com.onatsubasi.finalcase.user.application.service.ProductListService;
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
@RequestMapping("/api/customer/product-lists")
@Tag(name = "Customer Product Lists", description = "Current customer custom product list operations")
public class CustomerProductListController {

    private final ProductListService productListService;

    @Operation(
            summary = "List my product lists",
            description = "Lists current customer's non-deleted product lists.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductListResponse>>> listMyProductLists(
            @CurrentUser UserContext userContext
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(productListService.listMyProductLists(userContext))
        );
    }

    @Operation(
            summary = "Get my product list",
            description = "Returns an owned product list with product id references.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{listId}")
    public ResponseEntity<ApiResponse<ProductListResponse>> getMyProductList(
            @CurrentUser UserContext userContext,
            @PathVariable UUID listId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(productListService.getMyProductList(userContext, listId))
        );
    }

    @Operation(
            summary = "Create product list",
            description = "Creates a custom product list for current customer.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ProductListResponse>> createProductList(
            @CurrentUser UserContext userContext,
            @Valid @RequestBody CreateProductListRequest request
    ) {
        ProductListResponse response = productListService.createProductList(
                userContext,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product list created successfully", response));
    }

    @Operation(
            summary = "Update product list",
            description = "Updates an owned product list.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{listId}")
    public ResponseEntity<ApiResponse<ProductListResponse>> updateProductList(
            @CurrentUser UserContext userContext,
            @PathVariable UUID listId,
            @Valid @RequestBody UpdateProductListRequest request
    ) {
        ProductListResponse response = productListService.updateProductList(
                userContext,
                listId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Product list updated successfully", response)
        );
    }

    @Operation(
            summary = "Delete product list",
            description = "Soft-deletes an owned product list.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{listId}")
    public ResponseEntity<ApiResponse<Void>> deleteProductList(
            @CurrentUser UserContext userContext,
            @PathVariable UUID listId
    ) {
        productListService.deleteProductList(userContext, listId);

        return ResponseEntity.ok(
                ApiResponse.success("Product list deleted successfully")
        );
    }

    @Operation(
            summary = "Add product list item",
            description = "Adds a product id reference to an owned product list.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{listId}/items")
    public ResponseEntity<ApiResponse<ProductListResponse>> addItem(
            @CurrentUser UserContext userContext,
            @PathVariable UUID listId,
            @Valid @RequestBody AddProductListItemRequest request
    ) {
        ProductListResponse response = productListService.addItem(
                userContext,
                listId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Product list item added successfully", response)
        );
    }

    @Operation(
            summary = "Remove product list item",
            description = "Removes a product id reference from an owned product list.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{listId}/items/{productId}")
    public ResponseEntity<ApiResponse<ProductListResponse>> removeItem(
            @CurrentUser UserContext userContext,
            @PathVariable UUID listId,
            @PathVariable UUID productId
    ) {
        ProductListResponse response = productListService.removeItem(
                userContext,
                listId,
                productId
        );

        return ResponseEntity.ok(
                ApiResponse.success("Product list item removed successfully", response)
        );
    }
}
