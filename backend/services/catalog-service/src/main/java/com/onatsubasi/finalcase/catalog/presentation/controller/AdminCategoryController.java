package com.onatsubasi.finalcase.catalog.presentation.controller;

import com.onatsubasi.finalcase.catalog.application.dto.request.CreateCategoryRequest;
import com.onatsubasi.finalcase.catalog.application.dto.request.UpdateCategoryRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.CategoryResponse;
import com.onatsubasi.finalcase.catalog.application.service.CategoryService;
import com.onatsubasi.finalcase.catalog.application.support.CatalogPageResponseMapper;
import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;
import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
import com.onatsubasi.finalcase.catalog.domain.repository.CategoryQuery;
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
@RequestMapping("/api/admin/categories")
@Tag(name = "Category", description = "Admin category management")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final CatalogPageResponseMapper pageResponseMapper;

    @Operation(
            summary = "Create category",
            description = "Creates a root or child category. Slug is generated from name if omitted.",
            security = @SecurityRequirement(name = "bearerAuth")
    )

    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Category created successfully"
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
            description = "Category slug or path already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategoryResponse response = categoryService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    @Operation(
            summary = "Update category",
            description = "Updates category details and propagates category path/snapshot to descendants and products.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @Parameter(description = "Category id")
            @PathVariable UUID categoryId,

            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        CategoryResponse response = categoryService.update(categoryId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Category updated successfully", response)
        );
    }

    @Operation(
            summary = "Activate category",
            description = "Marks a suspended category as active.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{categoryId}/activate")
    public ResponseEntity<ApiResponse<CategoryResponse>> activate(
            @Parameter(description = "Category id")
            @PathVariable UUID categoryId
    ) {
        CategoryResponse response = categoryService.activate(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category activated successfully", response)
        );
    }

    @Operation(
            summary = "Suspend category",
            description = "Suspends a category if it is not used by non-deleted products.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{categoryId}/suspend")
    public ResponseEntity<ApiResponse<CategoryResponse>> suspend(
            @Parameter(description = "Category id")
            @PathVariable UUID categoryId
    ) {
        CategoryResponse response = categoryService.suspend(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category suspended successfully", response)
        );
    }

    @Operation(
            summary = "Deactivate category",
            description = "Backward-compatible alias for suspend category.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{categoryId}/deactivate")
    public ResponseEntity<ApiResponse<CategoryResponse>> deactivate(
            @Parameter(description = "Category id")
            @PathVariable UUID categoryId
    ) {
        CategoryResponse response = categoryService.deactivate(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category deactivated successfully", response)
        );
    }

    @Operation(
            summary = "Delete category",
            description = "Soft-deletes a leaf category if it is not used by non-deleted products.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Category id")
            @PathVariable UUID categoryId
    ) {
        categoryService.delete(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category deleted successfully")
        );
    }

    @Operation(
            summary = "Get category by id",
            description = "Returns a category by id.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            @Parameter(description = "Category id")
            @PathVariable UUID categoryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(categoryService.getById(categoryId))
        );
    }

    @Operation(
            summary = "List categories",
            description = "Lists categories with optional keyword, status, and parent filters.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiPageResponse<CategoryResponse>> list(
            @Parameter(description = "Keyword searched in category name, slug, path, or description")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Category status filter")
            @RequestParam(required = false) CatalogStatus status,

            @Parameter(description = "Parent category id")
            @RequestParam(required = false) UUID parentId,

            @Parameter(description = "Page number, zero-based")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size, max 100")
            @RequestParam(defaultValue = "20") int size
    ) {
        CatalogPage<CategoryResponse> result = categoryService.list(
                new CategoryQuery(keyword, status, parentId, page, size)
        );

        return ResponseEntity.ok(pageResponseMapper.toApiPageResponse(result));
    }
}