package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.domain.enums.InventoryItemStatus;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaInventoryItemRepositoryAdapter implements InventoryItemRepository {

    private final SpringDataInventoryItemJpaRepository springDataRepository;

    @Override
    public InventoryItem save(InventoryItem inventoryItem) {
        return springDataRepository.save(inventoryItem);
    }

    @Override
    public Optional<InventoryItem> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<InventoryItem> findByProductId(UUID productId) {
        return springDataRepository.findByProductId(productId);
    }

    @Override
    public Optional<InventoryItem> findByProductIdForUpdate(UUID productId) {
        return springDataRepository.findByProductIdForUpdate(productId);
    }

    @Override
    public List<InventoryItem> findAllByProductIdsForUpdate(Collection<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        return springDataRepository.findAllByProductIdsForUpdate(productIds);
    }

    @Override
    public boolean existsByProductId(UUID productId) {
        return springDataRepository.existsByProductId(productId);
    }

    @Override
    public List<InventoryItem> findByStatus(InventoryItemStatus status) {
        return springDataRepository.findByStatus(status);
    }
}