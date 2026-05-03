package com.onatsubasi.finalcase.checkout.infrastructure.persistence;

import com.onatsubasi.finalcase.checkout.domain.entity.ProcessedPaymentEvent;
import com.onatsubasi.finalcase.checkout.domain.repository.ProcessedPaymentEventRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaProcessedPaymentEventRepository
        implements ProcessedPaymentEventRepository {

    private final SpringDataProcessedPaymentEventJpaRepository springDataRepository;

    public JpaProcessedPaymentEventRepository(
            SpringDataProcessedPaymentEventJpaRepository springDataRepository
    ) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public ProcessedPaymentEvent save(ProcessedPaymentEvent event) {
        return springDataRepository.save(event);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return springDataRepository.existsByEventId(eventId);
    }

    @Override
    public Optional<ProcessedPaymentEvent> findByEventId(String eventId) {
        return springDataRepository.findByEventId(eventId);
    }
}