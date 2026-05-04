package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.entity.PaymentIdempotencyRecord;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentIdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPaymentIdempotencyRecordRepository
        implements PaymentIdempotencyRecordRepository {

    private final SpringDataPaymentIdempotencyRecordJpaRepository springDataRepository;

    @Override
    public PaymentIdempotencyRecord save(PaymentIdempotencyRecord record) {
        return springDataRepository.save(record);
    }

    @Override
    public Optional<PaymentIdempotencyRecord> findByIdempotencyKey(
            String idempotencyKey
    ) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<PaymentIdempotencyRecord> findByIdempotencyKeyForUpdate(
            String idempotencyKey
    ) {
        return springDataRepository.findByIdempotencyKeyForUpdate(idempotencyKey);
    }
}
