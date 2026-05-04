package com.onatsubasi.finalcase.payment.domain.repository;

import com.onatsubasi.finalcase.payment.domain.entity.PaymentIdempotencyRecord;

import java.util.Optional;

public interface PaymentIdempotencyRecordRepository {

    PaymentIdempotencyRecord save(PaymentIdempotencyRecord record);

    Optional<PaymentIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentIdempotencyRecord> findByIdempotencyKeyForUpdate(String idempotencyKey);
}