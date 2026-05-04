package com.onatsubasi.finalcase.shipment.domain.repository;

import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentIdempotencyRecord;

import java.util.Optional;

public interface ShipmentIdempotencyRecordRepository {

    ShipmentIdempotencyRecord save(ShipmentIdempotencyRecord record);

    Optional<ShipmentIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    Optional<ShipmentIdempotencyRecord> findByIdempotencyKeyForUpdate(String idempotencyKey);
}