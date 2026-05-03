package com.onatsubasi.finalcase.shipment.infrastructure.persistence;

import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentIdempotencyRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataShipmentIdempotencyRecordJpaRepository
        extends JpaRepository<ShipmentIdempotencyRecord, UUID> {

    Optional<ShipmentIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r from ShipmentIdempotencyRecord r
            where r.idempotencyKey = :idempotencyKey
           """)
    Optional<ShipmentIdempotencyRecord> findByIdempotencyKeyForUpdate(
            String idempotencyKey
    );
}