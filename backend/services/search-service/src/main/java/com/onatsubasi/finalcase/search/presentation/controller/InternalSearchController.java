package com.onatsubasi.finalcase.search.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.search.application.dto.response.ProductSearchDocumentResponse;
import com.onatsubasi.finalcase.search.application.dto.response.SearchRebuildResponse;
import com.onatsubasi.finalcase.search.application.service.SearchDocumentAdminService;
import com.onatsubasi.finalcase.search.application.service.SearchIndexRebuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/search")
@Tag(name = "Internal Search", description = "Internal search projection operations")
public class InternalSearchController {

    private final SearchDocumentAdminService searchDocumentAdminService;
    private final SearchIndexRebuildService searchIndexRebuildService;

    @Operation(
            summary = "Get internal search document",
            description = "Internal search document lookup by product id."
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
            summary = "Trigger internal search rebuild",
            description = "Internal endpoint for rebuilding search projection from Catalog snapshots."
    )
    @PostMapping("/rebuild")
    public ResponseEntity<ApiResponse<SearchRebuildResponse>> rebuildAll() {
        SearchRebuildResponse response = searchIndexRebuildService.rebuildAll();

        return ResponseEntity.ok(
                ApiResponse.success("Search index rebuild completed", response)
        );
    }
}
