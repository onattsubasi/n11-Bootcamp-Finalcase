package com.onatsubasi.finalcase.catalog.presentation.controller;

import com.onatsubasi.finalcase.catalog.application.dto.request.ProductSnapshotRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductSnapshotResponse;
import com.onatsubasi.finalcase.catalog.application.service.ProductService;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/catalog")
@Tag(name = "Internal Catalog", description = "Internal catalog endpoints for service-to-service communication")
public class InternalCatalogController {

    private final ProductService productService;

    @Operation(
            summary = "Get product snapshots",
            description = "Returns authoritative product snapshots for Checkout, Basket, and other internal services."
    )

    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product snapshots returned successfully"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "At least one requested product was not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )

    @PostMapping({"/products/snapshots", "/products/snapshot"})
    public ResponseEntity<ApiResponse<List<ProductSnapshotResponse>>> getProductSnapshots(
            @Valid @RequestBody ProductSnapshotRequest request
    ) {
        List<ProductSnapshotResponse> response = productService.getSnapshots(request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}