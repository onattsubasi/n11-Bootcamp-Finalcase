package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.entity.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaPaymentAttemptRepository implements PaymentAttemptRepository {

    private final SpringDataPaymentAttemptJpaRepository springDataRepository;

    @Override
    public PaymentAttempt save(PaymentAttempt attempt) {
        return springDataRepository.save(attempt);
    }

    @Override
    public Optional<PaymentAttempt> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<PaymentAttempt> findByProviderAndProviderToken(
            PaymentProviderCode provider,
            String providerToken
    ) {
        return springDataRepository.findByProviderAndProviderToken(
                provider,
                providerToken
        );
    }

    @Override
    public Optional<PaymentAttempt> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<PaymentAttempt> findByIdempotencyKeyForUpdate(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKeyForUpdate(idempotencyKey);
    }

    @Override
    public int countByPaymentId(UUID paymentId) {
        return springDataRepository.countByPayment_Id(paymentId);
    }
}