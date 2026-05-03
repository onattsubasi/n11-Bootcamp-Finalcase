package com.onatsubasi.finalcase.catalog.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductOwnerType;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductCatalogPayload(
        UUID productId,
        String sku,
        String name,
        String slug,
        String description,
        ProductStatus status,
        boolean sellable,
        MoneyPayload basePrice,
        BrandPayload brand,
        CategoryPayload category,
        OwnershipPayload ownership,
        List<ImagePayload> images,
        Map<String, String> attributes,
        String mainImageUrl,
        Instant createdAt,
        Instant updatedAt
) {

    public record MoneyPayload(
            BigDecimal amount,
            String currency
    ) {
    }

    public record BrandPayload(
            UUID id,
            String name,
            String slug
    ) {
    }

    public record CategoryPayload(
            UUID id,
            String name,
            String slug,
            String path
    ) {
    }

    public record OwnershipPayload(
            ProductOwnerType ownerType,
            String storeId,
            String storeName
    ) {
    }

    public record ImagePayload(
            String url,
            int sortOrder,
            boolean main
    ) {
    }

    public static ProductCatalogPayload from(Product product) {
        List<ImagePayload> images = product.getImages()
                .stream()
                .map(image -> new ImagePayload(
                        image.url(),
                        image.sortOrder(),
                        image.main()
                ))
                .toList();

        return new ProductCatalogPayload(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getStatus(),
                product.isSellable(),
                new MoneyPayload(
                        product.getBasePrice().getAmount(),
                        product.getBasePrice().getCurrency()
                ),
                new BrandPayload(
                        product.getBrand().getId(),
                        product.getBrand().getName(),
                        product.getBrand().getSlug()
                ),
                new CategoryPayload(
                        product.getCategory().getId(),
                        product.getCategory().getName(),
                        product.getCategory().getSlug(),
                        product.getCategory().getPath()
                ),
                new OwnershipPayload(
                        product.getOwnership().getOwnerType(),
                        product.getOwnership().getStoreId(),
                        product.getOwnership().getStoreName()
                ),
                images,
                Map.copyOf(product.getAttributes()),
                mainImageUrl(images),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private static String mainImageUrl(List<ImagePayload> images) {
        return images.stream()
                .filter(ImagePayload::main)
                .findFirst()
                .or(() -> images.stream().findFirst())
                .map(ImagePayload::url)
                .orElse(null);
    }
}