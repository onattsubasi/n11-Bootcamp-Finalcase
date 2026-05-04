package com.onatsubasi.finalcase.inventory.domain.repository;

import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository {

    StockReservation save(StockReservation reservation);

    Optional<StockReservation> findById(UUID id);

    Optional<StockReservation> findByIdForUpdate(UUID id);

    Optional<StockReservation> findByIdempotencyKey(String idempotencyKey);

    List<StockReservation> findExpiredReservationsForUpdate(
            StockReservationStatus status,
            Instant now,
            int batchSize
    );
}