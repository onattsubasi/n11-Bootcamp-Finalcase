package com.onatsubasi.finalcase.catalog.domain.repository;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;
import com.onatsubasi.finalcase.catalog.domain.valueobject.BrandSnapshot;
import com.onatsubasi.finalcase.catalog.domain.valueobject.CategorySnapshot;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    List<Product> saveAll(Collection<Product> products);

    Optional<Product> findById(UUID id);

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    List<Product> findByIds(Collection<UUID> ids);

    List<Product> findActiveByIds(Collection<UUID> ids);

    List<Product> findByCategoryIdAndStatusNot(UUID categoryId, ProductStatus status);

    List<Product> findByBrandIdAndStatusNot(UUID brandId, ProductStatus status);

    CatalogPage<Product> findAll(ProductQuery query);

    boolean existsBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySkuAndIdNot(String sku, UUID productId);

    boolean existsBySlugAndIdNot(String slug, UUID productId);

    long countByCategoryIdAndStatusNot(UUID categoryId, ProductStatus status);

    long countByBrandIdAndStatusNot(UUID brandId, ProductStatus status);

    void updateCategorySnapshot(UUID categoryId, CategorySnapshot snapshot);

    void updateBrandSnapshot(UUID brandId, BrandSnapshot snapshot);
}