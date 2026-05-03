package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.domain.entity.InventoryProcessedEvent;
import com.onatsubasi.finalcase.inventory.domain.repository.InventoryProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaInventoryProcessedEventRepositoryAdapter implements InventoryProcessedEventRepository {

    private final SpringDataInventoryProcessedEventJpaRepository springDataRepository;

    @Override
    public InventoryProcessedEvent save(InventoryProcessedEvent event) {
        return springDataRepository.save(event);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return springDataRepository.existsByEventId(eventId);
    }
}