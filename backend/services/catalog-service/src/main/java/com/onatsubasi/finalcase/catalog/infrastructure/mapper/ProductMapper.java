package com.onatsubasi.finalcase.catalog.infrastructure.mapper;

import com.onatsubasi.finalcase.catalog.application.dto.request.ProductImageRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductResponse;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductSnapshotResponse;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductSummaryResponse;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;
import com.onatsubasi.finalcase.catalog.domain.valueobject.CategoryAncestor;
import com.onatsubasi.finalcase.catalog.domain.valueobject.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProductMapper {

    public ProductImage toImage(ProductImageRequest request) {
        return new ProductImage(
                request.url(),
                request.sortOrder() == null ? 0 : request.sortOrder(),
                Boolean.TRUE.equals(request.main())
        );
    }

    public List<ProductImage> toImages(List<ProductImageRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        return requests.stream()
                .map(this::toImage)
                .toList();
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                new ProductResponse.ProductOwnershipResponse(
                        product.getOwnership().getOwnerType(),
                        product.getOwnership().getStoreId(),
                        product.getOwnership().getStoreName()
                ),
                new ProductResponse.MoneyResponse(
                        product.getBasePrice().getAmount(),
                        product.getBasePrice().getCurrency()
                ),
                new ProductResponse.BrandSnapshotResponse(
                        product.getBrand().getId(),
                        product.getBrand().getName(),
                        product.getBrand().getSlug()
                ),
                new ProductResponse.CategorySnapshotResponse(
                        product.getCategory().getId(),
                        product.getCategory().getName(),
                        product.getCategory().getSlug(),
                        product.getCategory().getPath(),
                        mapAncestors(product.getCategory().getAncestors())
                ),
                product.getImages()
                        .stream()
                        .map(image -> new ProductResponse.ProductImageResponse(
                                image.url(),
                                image.sortOrder(),
                                image.main()
                        ))
                        .toList(),
                Map.copyOf(product.getAttributes()),
                product.getStatus(),
                product.isSellable(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public ProductSummaryResponse toSummaryResponse(Product product) {
        return new ProductSummaryResponse(
                product.getId().toString(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getBasePrice().getAmount(),
                product.getBasePrice().getCurrency(),
                product.getBrand().getId().toString(),
                product.getBrand().getName(),
                product.getCategory().getId().toString(),
                product.getCategory().getName(),
                mainImageUrl(product),
                product.getStatus(),
                product.getUpdatedAt()
        );
    }

    public ProductSnapshotResponse toSnapshotResponse(Product product) {
        return new ProductSnapshotResponse(
                product.getId().toString(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getStatus(),
                product.isSellable(),
                new ProductSnapshotResponse.MoneySnapshot(
                        product.getBasePrice().getAmount(),
                        product.getBasePrice().getCurrency()
                ),
                new ProductSnapshotResponse.BrandSnapshot(
                        product.getBrand().getId().toString(),
                        product.getBrand().getName(),
                        product.getBrand().getSlug()
                ),
                new ProductSnapshotResponse.CategorySnapshot(
                        product.getCategory().getId().toString(),
                        product.getCategory().getName(),
                        product.getCategory().getSlug(),
                        product.getCategory().getPath()
                ),
                new ProductSnapshotResponse.OwnershipSnapshot(
                        product.getOwnership().getOwnerType(),
                        product.getOwnership().getStoreId(),
                        product.getOwnership().getStoreName()
                ),
                mainImageUrl(product)
        );
    }

    private List<ProductResponse.CategoryAncestorResponse> mapAncestors(
            List<CategoryAncestor> ancestors
    ) {
        if (ancestors == null || ancestors.isEmpty()) {
            return List.of();
        }

        return ancestors.stream()
                .map(ancestor -> new ProductResponse.CategoryAncestorResponse(
                        ancestor.id(),
                        ancestor.name(),
                        ancestor.slug()
                ))
                .toList();
    }

    private String mainImageUrl(Product product) {
        return product.getImages()
                .stream()
                .filter(ProductImage::main)
                .findFirst()
                .or(() -> product.getImages().stream().findFirst())
                .map(ProductImage::url)
                .orElse(null);
    }
}