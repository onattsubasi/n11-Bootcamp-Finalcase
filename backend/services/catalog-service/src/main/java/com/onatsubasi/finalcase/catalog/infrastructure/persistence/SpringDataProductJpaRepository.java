package com.onatsubasi.finalcase.catalog.infrastructure.persistence;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductJpaRepository
        extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    List<Product> findByIdIn(Collection<UUID> ids);

    List<Product> findByIdInAndStatus(Collection<UUID> ids, ProductStatus status);

    boolean existsBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySkuAndIdNot(String sku, UUID id);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Query("""
           select p
             from Product p
            where p.category.id = :categoryId
              and p.status <> :status
           """)
    List<Product> findByCategoryIdAndStatusNot(
            @Param("categoryId") UUID categoryId,
            @Param("status") ProductStatus status
    );

    @Query("""
           select p
             from Product p
            where p.brand.id = :brandId
              and p.status <> :status
           """)
    List<Product> findByBrandIdAndStatusNot(
            @Param("brandId") UUID brandId,
            @Param("status") ProductStatus status
    );

    @Query("""
           select count(p)
             from Product p
            where p.category.id = :categoryId
              and p.status <> :status
           """)
    long countByCategoryIdAndStatusNot(
            @Param("categoryId") UUID categoryId,
            @Param("status") ProductStatus status
    );

    @Query("""
           select count(p)
             from Product p
            where p.brand.id = :brandId
              and p.status <> :status
           """)
    long countByBrandIdAndStatusNot(
            @Param("brandId") UUID brandId,
            @Param("status") ProductStatus status
    );
}