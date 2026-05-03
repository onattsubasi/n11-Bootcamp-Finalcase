package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.domain.entity.InventoryProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataInventoryProcessedEventJpaRepository
        extends JpaRepository<InventoryProcessedEvent, UUID> {

    boolean existsByEventId(String eventId);
}