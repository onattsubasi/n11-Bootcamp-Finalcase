package com.onatsubasi.finalcase.catalog.application.dto.response;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductOwnerType;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;

import java.math.BigDecimal;

public record ProductSnapshotResponse(
        String productId,
        String sku,
        String name,
        String slug,
        ProductStatus status,
        boolean sellable,
        MoneySnapshot basePrice,
        BrandSnapshot brand,
        CategorySnapshot category,
        OwnershipSnapshot ownership,
        String mainImageUrl
) {

    public record MoneySnapshot(
            BigDecimal amount,
            String currency
    ) {
    }

    public record BrandSnapshot(
            String id,
            String name,
            String slug
    ) {
    }

    public record CategorySnapshot(
            String id,
            String name,
            String slug,
            String path
    ) {
    }

    public record OwnershipSnapshot(
            ProductOwnerType ownerType,
            String storeId,
            String storeName
    ) {
    }
}