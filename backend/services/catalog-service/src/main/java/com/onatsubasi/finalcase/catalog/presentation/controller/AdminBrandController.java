package com.onatsubasi.finalcase.catalog.presentation.controller;

import com.onatsubasi.finalcase.catalog.application.dto.request.CreateBrandRequest;
import com.onatsubasi.finalcase.catalog.application.dto.request.UpdateBrandRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.BrandResponse;
import com.onatsubasi.finalcase.catalog.application.service.BrandService;
import com.onatsubasi.finalcase.catalog.application.support.CatalogPageResponseMapper;
import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;
import com.onatsubasi.finalcase.catalog.domain.repository.BrandQuery;
import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
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
@RequestMapping("/api/admin/brands")
@Tag(name = "Brand", description = "Admin brand management")
public class AdminBrandController {

    private final BrandService brandService;
    private final CatalogPageResponseMapper pageResponseMapper;

    @Operation(
            summary = "Create brand",
            description = "Creates a brand. Slug is generated from name if omitted.",
            security = @SecurityRequirement(name = "bearerAuth")
    )

    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Brand created successfully"
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
            description = "Brand slug already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> create(
            @Valid @RequestBody CreateBrandRequest request
    ) {
        BrandResponse response = brandService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Brand created successfully", response));
    }

    @Operation(
            summary = "Update brand",
            description = "Updates brand details and propagates brand snapshot to related products.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{brandId}")
    public ResponseEntity<ApiResponse<BrandResponse>> update(
            @Parameter(description = "Brand id")
            @PathVariable UUID brandId,

            @Valid @RequestBody UpdateBrandRequest request
    ) {
        BrandResponse response = brandService.update(brandId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Brand updated successfully", response)
        );
    }

    @Operation(
            summary = "Activate brand",
            description = "Marks a suspended brand as active.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{brandId}/activate")
    public ResponseEntity<ApiResponse<BrandResponse>> activate(
            @Parameter(description = "Brand id")
            @PathVariable UUID brandId
    ) {
        BrandResponse response = brandService.activate(brandId);

        return ResponseEntity.ok(
                ApiResponse.success("Brand activated successfully", response)
        );
    }

    @Operation(
            summary = "Suspend brand",
            description = "Suspends a brand if it is not used by non-deleted products.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{brandId}/suspend")
    public ResponseEntity<ApiResponse<BrandResponse>> suspend(
            @Parameter(description = "Brand id")
            @PathVariable UUID brandId
    ) {
        BrandResponse response = brandService.suspend(brandId);

        return ResponseEntity.ok(
                ApiResponse.success("Brand suspended successfully", response)
        );
    }

    @Operation(
            summary = "Deactivate brand",
            description = "Backward-compatible alias for suspend brand.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{brandId}/deactivate")
    public ResponseEntity<ApiResponse<BrandResponse>> deactivate(
            @Parameter(description = "Brand id")
            @PathVariable UUID brandId
    ) {
        BrandResponse response = brandService.deactivate(brandId);

        return ResponseEntity.ok(
                ApiResponse.success("Brand deactivated successfully", response)
        );
    }

    @Operation(
            summary = "Delete brand",
            description = "Soft-deletes a brand if it is not used by non-deleted products.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{brandId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Brand id")
            @PathVariable UUID brandId
    ) {
        brandService.delete(brandId);

        return ResponseEntity.ok(
                ApiResponse.success("Brand deleted successfully")
        );
    }

    @Operation(
            summary = "Get brand by id",
            description = "Returns a brand by id.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{brandId}")
    public ResponseEntity<ApiResponse<BrandResponse>> getById(
            @Parameter(description = "Brand id")
            @PathVariable UUID brandId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(brandService.getById(brandId))
        );
    }

    @Operation(
            summary = "List brands",
            description = "Lists brands with optional keyword and status filters.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiPageResponse<BrandResponse>> list(
            @Parameter(description = "Keyword searched in brand name, slug, or description")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Brand status filter")
            @RequestParam(required = false) CatalogStatus status,

            @Parameter(description = "Page number, zero-based")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size, max 100")
            @RequestParam(defaultValue = "20") int size
    ) {
        CatalogPage<BrandResponse> result = brandService.list(
                new BrandQuery(keyword, status, page, size)
        );

        return ResponseEntity.ok(pageResponseMapper.toApiPageResponse(result));
    }
}