package com.onatsubasi.finalcase.shipment.infrastructure.persistence;

import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentIdempotencyRecord;
import com.onatsubasi.finalcase.shipment.domain.repository.ShipmentIdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaShipmentIdempotencyRecordRepository
        implements ShipmentIdempotencyRecordRepository {

    private final SpringDataShipmentIdempotencyRecordJpaRepository springDataRepository;

    @Override
    public ShipmentIdempotencyRecord save(ShipmentIdempotencyRecord record) {
        return springDataRepository.save(record);
    }

    @Override
    public Optional<ShipmentIdempotencyRecord> findByIdempotencyKey(
            String idempotencyKey
    ) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<ShipmentIdempotencyRecord> findByIdempotencyKeyForUpdate(
            String idempotencyKey
    ) {
        return springDataRepository.findByIdempotencyKeyForUpdate(idempotencyKey);
    }
}