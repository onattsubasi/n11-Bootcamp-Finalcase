package com.onatsubasi.finalcase.checkout.domain.repository;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutIdempotencyRecord;

import java.util.Optional;

public interface CheckoutIdempotencyRecordRepository {

    CheckoutIdempotencyRecord save(CheckoutIdempotencyRecord record);

    Optional<CheckoutIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    Optional<CheckoutIdempotencyRecord> findByIdempotencyKeyForUpdate(String idempotencyKey);
}