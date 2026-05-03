package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.model.PaymentCancellation;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentCancellationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaPaymentCancellationRepository implements PaymentCancellationRepository {

    private final SpringDataPaymentCancellationJpaRepository springDataRepository;

    @Override
    public PaymentCancellation save(PaymentCancellation cancellation) {
        return springDataRepository.save(cancellation);
    }

    @Override
    public Optional<PaymentCancellation> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<PaymentCancellation> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<PaymentCancellation> findByIdempotencyKeyForUpdate(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKeyForUpdate(idempotencyKey);
    }
}
