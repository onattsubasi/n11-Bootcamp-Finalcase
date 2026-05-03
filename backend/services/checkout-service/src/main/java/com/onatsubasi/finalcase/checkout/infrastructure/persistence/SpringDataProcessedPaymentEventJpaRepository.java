package com.onatsubasi.finalcase.checkout.infrastructure.persistence;

import com.onatsubasi.finalcase.checkout.domain.entity.ProcessedPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataProcessedPaymentEventJpaRepository
        extends JpaRepository<ProcessedPaymentEvent, UUID> {

    boolean existsByEventId(String eventId);

    Optional<ProcessedPaymentEvent> findByEventId(String eventId);
}