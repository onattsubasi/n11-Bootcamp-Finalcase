package com.onatsubasi.finalcase.inventory.domain.repository;

import com.onatsubasi.finalcase.inventory.domain.entity.InventoryProcessedEvent;

public interface InventoryProcessedEventRepository {

    InventoryProcessedEvent save(InventoryProcessedEvent event);

    boolean existsByEventId(String eventId);
}