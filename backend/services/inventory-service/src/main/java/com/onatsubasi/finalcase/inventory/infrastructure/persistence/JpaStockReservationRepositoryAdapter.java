package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;
import com.onatsubasi.finalcase.inventory.domain.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaStockReservationRepositoryAdapter implements StockReservationRepository {

    private final SpringDataStockReservationJpaRepository springDataRepository;

    @Override
    public StockReservation save(StockReservation reservation) {
        return springDataRepository.save(reservation);
    }

    @Override
    public Optional<StockReservation> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<StockReservation> findByIdForUpdate(UUID id) {
        return springDataRepository.findByIdForUpdate(id);
    }

    @Override
    public Optional<StockReservation> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public List<StockReservation> findExpiredReservationsForUpdate(
            StockReservationStatus status,
            Instant now,
            int batchSize
    ) {
        return springDataRepository.findExpiredReservationsForUpdate(
                status,
                now,
                PageRequest.of(0, Math.max(batchSize, 1))
        );
    }
}