package com.onatsubasi.finalcase.search.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.search.application.dto.response.ProductSearchDocumentResponse;
import com.onatsubasi.finalcase.search.application.dto.response.SearchRebuildResponse;
import com.onatsubasi.finalcase.search.application.service.SearchDocumentAdminService;
import com.onatsubasi.finalcase.search.application.service.SearchIndexRebuildService;
import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/search")
@Tag(name = "Admin Search", description = "Admin search projection inspection and rebuild operations")
public class AdminSearchController {

    private final SearchDocumentAdminService searchDocumentAdminService;
    private final SearchIndexRebuildService searchIndexRebuildService;

    @Operation(
            summary = "Get search document by product id",
            description = "Returns the denormalized search document for a product.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/documents/{productId}")
    public ResponseEntity<ApiResponse<ProductSearchDocumentResponse>> getByProductId(
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(searchDocumentAdminService.getByProductId(productId))
        );
    }

    @Operation(
            summary = "List search documents by status",
            description = "Lists search projection documents by status. Defaults to ACTIVE.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<ProductSearchDocumentResponse>>> listByStatus(
            @RequestParam(required = false) ProductSearchStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(searchDocumentAdminService.listByStatus(status))
        );
    }

    @Operation(
            summary = "Rebuild search index",
            description = "Rebuilds product search documents from Catalog Service paginated snapshots.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/rebuild")
    public ResponseEntity<ApiResponse<SearchRebuildResponse>> rebuildAll() {
        SearchRebuildResponse response = searchIndexRebuildService.rebuildAll();

        return ResponseEntity.ok(
                ApiResponse.success("Search index rebuild completed", response)
        );
    }
}
