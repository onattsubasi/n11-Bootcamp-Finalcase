package com.onatsubasi.finalcase.search.infrastructure.persistence;

import com.onatsubasi.finalcase.search.domain.entity.ProcessedSearchEvent;
import com.onatsubasi.finalcase.search.domain.repository.ProcessedSearchEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaProcessedSearchEventRepositoryAdapter implements ProcessedSearchEventRepository {

    private final SpringDataProcessedSearchEventJpaRepository springDataRepository;

    @Override
    public ProcessedSearchEvent save(ProcessedSearchEvent event) {
        return springDataRepository.save(event);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return springDataRepository.existsByEventId(eventId);
    }

    @Override
    public Optional<ProcessedSearchEvent> findByEventId(String eventId) {
        return springDataRepository.findByEventId(eventId);
    }
}
