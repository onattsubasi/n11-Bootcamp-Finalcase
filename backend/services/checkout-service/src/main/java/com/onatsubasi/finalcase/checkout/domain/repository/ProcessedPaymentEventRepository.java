package com.onatsubasi.finalcase.checkout.domain.repository;

import com.onatsubasi.finalcase.checkout.domain.entity.ProcessedPaymentEvent;

import java.util.Optional;

public interface ProcessedPaymentEventRepository {

    ProcessedPaymentEvent save(ProcessedPaymentEvent event);

    boolean existsByEventId(String eventId);

    Optional<ProcessedPaymentEvent> findByEventId(String eventId);
}