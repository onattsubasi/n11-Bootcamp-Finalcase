package com.onatsubasi.finalcase.payment.domain.repository;


import com.onatsubasi.finalcase.payment.domain.entity.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAttemptRepository {

    PaymentAttempt save(PaymentAttempt attempt);

    Optional<PaymentAttempt> findById(UUID id);

    Optional<PaymentAttempt> findByProviderAndProviderToken(
            PaymentProviderCode provider,
            String providerToken
    );

    Optional<PaymentAttempt> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentAttempt> findByIdempotencyKeyForUpdate(String idempotencyKey);

    int countByPaymentId(UUID paymentId);
}