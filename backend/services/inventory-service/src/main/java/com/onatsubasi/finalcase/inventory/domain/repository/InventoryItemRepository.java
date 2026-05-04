package com.onatsubasi.finalcase.inventory.domain.repository;

import com.onatsubasi.finalcase.inventory.domain.enums.InventoryItemStatus;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository {

    InventoryItem save(InventoryItem inventoryItem);

    Optional<InventoryItem> findById(UUID id);

    Optional<InventoryItem> findByProductId(UUID productId);

    Optional<InventoryItem> findByProductIdForUpdate(UUID productId);

    List<InventoryItem> findAllByProductIdsForUpdate(Collection<UUID> productIds);

    boolean existsByProductId(UUID productId);

    List<InventoryItem> findByStatus(InventoryItemStatus status);
}