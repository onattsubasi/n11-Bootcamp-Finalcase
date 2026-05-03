package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.AbstractIntegrationTest;
import com.onatsubasi.finalcase.inventory.domain.enums.StockMovementType;
import com.onatsubasi.finalcase.inventory.domain.entity.StockMovement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class StockMovementRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JpaStockMovementRepositoryAdapter stockMovementRepository;

    @Test
    @DisplayName("stock movement audit entries are persisted")
    void shouldSaveStockMovement() {
        UUID inventoryItemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockMovement movement = StockMovement.create(
                inventoryItemId,
                productId,
                StockMovementType.ADMIN_INCREASE,
                50,
                50,
                0,
                100,
                0,
                null,
                null,
                null,
                "Supplier delivery",
                "admin-user"
        );

        StockMovement saved = stockMovementRepository.save(movement);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getInventoryItemId()).isEqualTo(inventoryItemId);
        assertThat(saved.getMovementType()).isEqualTo(StockMovementType.ADMIN_INCREASE);
    }
}
