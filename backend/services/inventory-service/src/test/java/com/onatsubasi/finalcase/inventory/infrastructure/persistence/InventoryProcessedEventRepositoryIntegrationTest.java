package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.AbstractIntegrationTest;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryProcessedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class InventoryProcessedEventRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JpaInventoryProcessedEventRepositoryAdapter processedEventRepository;

    @Test
    @DisplayName("processed event ids are persisted uniquely for consumer idempotency")
    void shouldSaveAndCheckExists() {
        String eventId = UUID.randomUUID().toString();
        InventoryProcessedEvent event = InventoryProcessedEvent.markProcessed(eventId, "inventory.stock.updated");

        processedEventRepository.save(event);

        assertThat(processedEventRepository.existsByEventId(eventId)).isTrue();
    }
}
