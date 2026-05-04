package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogProductSnapshotClientResponse(
        String productId,
        String sku,
        String name,
        String slug,
        String status,
        boolean sellable,
        MoneySnapshot basePrice,
        BrandSnapshot brand,
        CategorySnapshot category,
        OwnershipSnapshot ownership,
        String mainImageUrl
) {
    public BigDecimal price() {
        return basePrice == null ? BigDecimal.ZERO : basePrice.amount();
    }

    public String currency() {
        return basePrice == null ? null : basePrice.currency();
    }

    public boolean active() {
        return sellable;
    }

    public String brandId() {
        return brand == null ? null : brand.id();
    }

    public String brandName() {
        return brand == null ? null : brand.name();
    }

    public String categoryId() {
        return category == null ? null : category.id();
    }

    public String categoryName() {
        return category == null ? null : category.name();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MoneySnapshot(
            BigDecimal amount,
            String currency
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BrandSnapshot(
            String id,
            String name,
            String slug
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategorySnapshot(
            String id,
            String name,
            String slug,
            String path
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OwnershipSnapshot(
            String ownerType,
            String storeId,
            String storeName
    ) {
    }
}
