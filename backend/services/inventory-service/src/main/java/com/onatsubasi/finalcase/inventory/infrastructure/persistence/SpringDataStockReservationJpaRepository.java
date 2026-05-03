package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataStockReservationJpaRepository extends JpaRepository<StockReservation, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<StockReservation> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = "items")
    Optional<StockReservation> findById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r
             from StockReservation r
            where r.id = :reservationId
           """)
    Optional<StockReservation> findByIdForUpdate(@Param("reservationId") UUID reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r
             from StockReservation r
            where r.status = :status
              and r.reservedUntil < :now
            order by r.reservedUntil asc
           """)
    List<StockReservation> findExpiredReservationsForUpdate(
            @Param("status") StockReservationStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );
}