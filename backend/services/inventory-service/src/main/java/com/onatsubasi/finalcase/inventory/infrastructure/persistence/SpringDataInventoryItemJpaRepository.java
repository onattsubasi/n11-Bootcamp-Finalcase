package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.domain.enums.InventoryItemStatus;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataInventoryItemJpaRepository extends JpaRepository<InventoryItem, UUID> {

    Optional<InventoryItem> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);

    List<InventoryItem> findByStatus(InventoryItemStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select i
             from InventoryItem i
            where i.productId = :productId
           """)
    Optional<InventoryItem> findByProductIdForUpdate(@Param("productId") UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select i
             from InventoryItem i
            where i.productId in :productIds
            order by i.productId asc
           """)
    List<InventoryItem> findAllByProductIdsForUpdate(
            @Param("productIds") Collection<UUID> productIds
    );
}