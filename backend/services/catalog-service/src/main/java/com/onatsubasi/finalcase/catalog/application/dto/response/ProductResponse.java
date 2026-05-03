package com.onatsubasi.finalcase.catalog.application.dto.response;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductOwnerType;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Detailed product response")
public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String slug,
        String description,
        ProductOwnershipResponse ownership,
        MoneyResponse basePrice,
        BrandSnapshotResponse brand,
        CategorySnapshotResponse category,
        List<ProductImageResponse> images,
        Map<String, String> attributes,
        ProductStatus status,
        boolean sellable,
        Instant createdAt,
        Instant updatedAt
) {

    public record MoneyResponse(
            BigDecimal amount,
            String currency
    ) {
    }

    public record ProductOwnershipResponse(
            ProductOwnerType ownerType,
            String storeId,
            String storeName
    ) {
    }

    public record BrandSnapshotResponse(
            UUID id,
            String name,
            String slug
    ) {
    }

    public record CategorySnapshotResponse(
            UUID id,
            String name,
            String slug,
            String path,
            List<CategoryAncestorResponse> ancestors
    ) {
    }

    public record CategoryAncestorResponse(
            UUID id,
            String name,
            String slug
    ) {
    }

    public record ProductImageResponse(
            String url,
            int sortOrder,
            boolean main
    ) {
    }
}