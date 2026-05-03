package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.AbstractIntegrationTest;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.repository.InventoryItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class InventoryItemRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Test
    @DisplayName("Should save and find inventory item by productId")
    void shouldSaveAndFindByProductId() {
        // Given
        UUID productId = UUID.randomUUID();
        InventoryItem item = InventoryItem.create(productId, 100, 10);

        // When
        inventoryItemRepository.save(item);
        Optional<InventoryItem> found = inventoryItemRepository.findByProductId(productId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getProductId()).isEqualTo(productId);
        assertThat(found.get().getTotalQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should lock inventory item for update")
    void shouldLockForUpdate() {
        // Given
        UUID productId = UUID.randomUUID();
        InventoryItem item = InventoryItem.create(productId, 100, 10);
        inventoryItemRepository.save(item);

        // When
        Optional<InventoryItem> locked = inventoryItemRepository.findByProductIdForUpdate(productId);

        // Then
        assertThat(locked).isPresent();
    }
}
