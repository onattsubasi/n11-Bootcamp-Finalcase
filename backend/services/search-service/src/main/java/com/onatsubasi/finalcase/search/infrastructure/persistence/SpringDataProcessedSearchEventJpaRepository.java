package com.onatsubasi.finalcase.search.infrastructure.persistence;

import com.onatsubasi.finalcase.search.domain.entity.ProcessedSearchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataProcessedSearchEventJpaRepository
        extends JpaRepository<ProcessedSearchEvent, UUID> {

    boolean existsByEventId(String eventId);

    Optional<ProcessedSearchEvent> findByEventId(String eventId);
}