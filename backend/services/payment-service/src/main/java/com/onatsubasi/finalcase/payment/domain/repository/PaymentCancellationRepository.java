package com.onatsubasi.finalcase.payment.domain.repository;

import com.onatsubasi.finalcase.payment.domain.entity.PaymentCancellation;

import java.util.Optional;
import java.util.UUID;

public interface PaymentCancellationRepository {

    PaymentCancellation save(PaymentCancellation cancellation);

    Optional<PaymentCancellation> findById(UUID id);

    Optional<PaymentCancellation> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentCancellation> findByIdempotencyKeyForUpdate(String idempotencyKey);
}
