package com.onatsubasi.finalcase.catalog.presentation.controller;

import com.onatsubasi.finalcase.catalog.application.dto.request.CreateProductRequest;
import com.onatsubasi.finalcase.catalog.application.dto.request.UpdateProductRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductResponse;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductSummaryResponse;
import com.onatsubasi.finalcase.catalog.application.service.ProductService;
import com.onatsubasi.finalcase.catalog.application.support.CatalogPageResponseMapper;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductQuery;
import com.onatsubasi.finalcase.common.core.response.ApiPageResponse;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Product Catalog", description = "Admin product management")
public class AdminProductController {

    private final ProductService productService;
    private final CatalogPageResponseMapper pageResponseMapper;

    @Operation(
            summary = "Create product",
            description = "Creates a draft product. If publish=true, activates it immediately.",
            security = @SecurityRequirement(name = "bearerAuth")
    )

    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Product created successfully"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Product SKU or slug already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = productService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", response));
    }

    @Operation(
            summary = "Update product",
            description = "Updates product details, price, brand/category snapshots, images, and attributes.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @Parameter(description = "Product id")
            @PathVariable UUID productId,

            @Valid @RequestBody UpdateProductRequest request
    ) {
        ProductResponse response = productService.update(productId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", response)
        );
    }

    @Operation(
            summary = "Activate product",
            description = "Activates a product if it satisfies activation rules.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{productId}/activate")
    public ResponseEntity<ApiResponse<ProductResponse>> activate(
            @Parameter(description = "Product id")
            @PathVariable UUID productId
    ) {
        ProductResponse response = productService.activate(productId);

        return ResponseEntity.ok(
                ApiResponse.success("Product activated successfully", response)
        );
    }

    @Operation(
            summary = "Suspend product",
            description = "Suspends an active product.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{productId}/suspend")
    public ResponseEntity<ApiResponse<ProductResponse>> suspend(
            @Parameter(description = "Product id")
            @PathVariable UUID productId
    ) {
        ProductResponse response = productService.suspend(productId);

        return ResponseEntity.ok(
                ApiResponse.success("Product suspended successfully", response)
        );
    }

    @Operation(
            summary = "Delete product",
            description = "Soft-deletes a product.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Product id")
            @PathVariable UUID productId
    ) {
        productService.delete(productId);

        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully")
        );
    }

    @Operation(
            summary = "Get product by id",
            description = "Returns detailed product information by id.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(
            @Parameter(description = "Product id")
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getById(productId))
        );
    }

    @Operation(
            summary = "List products",
            description = "Lists products with keyword, status, category, brand, and store filters.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiPageResponse<ProductSummaryResponse>> list(
            @Parameter(description = "Keyword searched in product name, sku, slug, or description")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Product status filter")
            @RequestParam(required = false) ProductStatus status,

            @Parameter(description = "Category id filter")
            @RequestParam(required = false) UUID categoryId,

            @Parameter(description = "Brand id filter")
            @RequestParam(required = false) UUID brandId,

            @Parameter(description = "Store id filter. Reserved for future multi-vendor expansion.")
            @RequestParam(required = false) String storeId,

            @Parameter(description = "Page number, zero-based")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size, max 100")
            @RequestParam(defaultValue = "20") int size
    ) {
        CatalogPage<ProductSummaryResponse> result = productService.list(
                new ProductQuery(
                        keyword,
                        status,
                        categoryId,
                        brandId,
                        storeId,
                        page,
                        size
                )
        );

        return ResponseEntity.ok(pageResponseMapper.toApiPageResponse(result));
    }
}