package com.onatsubasi.finalcase.search.domain.repository;

import com.onatsubasi.finalcase.search.domain.entity.ProcessedSearchEvent;

import java.util.Optional;

public interface ProcessedSearchEventRepository {

    ProcessedSearchEvent save(ProcessedSearchEvent event);

    boolean existsByEventId(String eventId);

    Optional<ProcessedSearchEvent> findByEventId(String eventId);
}