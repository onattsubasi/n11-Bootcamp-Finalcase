package com.onatsubasi.finalcase.search.infrastructure.persistence;

import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductSearchDocumentJpaRepository
        extends JpaRepository<ProductSearchDocument, UUID> {

    Optional<ProductSearchDocument> findByProductId(UUID productId);

    List<ProductSearchDocument> findByStatus(ProductSearchStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select d
             from ProductSearchDocument d
            where d.productId = :productId
           """)
    Optional<ProductSearchDocument> findByProductIdForUpdate(@Param("productId") UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select d
             from ProductSearchDocument d
            where d.productId in :productIds
            order by d.productId asc
           """)
    List<ProductSearchDocument> findByProductIdsForUpdate(
            @Param("productIds") Collection<UUID> productIds
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select d
             from ProductSearchDocument d
            where d.categoryId = :categoryId
            order by d.productId asc
           """)
    List<ProductSearchDocument> findByCategoryIdForUpdate(@Param("categoryId") UUID categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select d
             from ProductSearchDocument d
            where d.brandId = :brandId
            order by d.productId asc
           """)
    List<ProductSearchDocument> findByBrandIdForUpdate(@Param("brandId") UUID brandId);
}
