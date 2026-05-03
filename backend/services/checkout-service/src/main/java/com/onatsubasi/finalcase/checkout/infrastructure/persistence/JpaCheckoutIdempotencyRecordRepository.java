package com.onatsubasi.finalcase.checkout.infrastructure.persistence;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutIdempotencyRecord;
import com.onatsubasi.finalcase.checkout.domain.repository.CheckoutIdempotencyRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCheckoutIdempotencyRecordRepository
        implements CheckoutIdempotencyRecordRepository {

    private final SpringDataCheckoutIdempotencyRecordJpaRepository springDataRepository;

    public JpaCheckoutIdempotencyRecordRepository(
            SpringDataCheckoutIdempotencyRecordJpaRepository springDataRepository
    ) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public CheckoutIdempotencyRecord save(CheckoutIdempotencyRecord record) {
        return springDataRepository.save(record);
    }

    @Override
    public Optional<CheckoutIdempotencyRecord> findByIdempotencyKey(
            String idempotencyKey
    ) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<CheckoutIdempotencyRecord> findByIdempotencyKeyForUpdate(
            String idempotencyKey
    ) {
        return springDataRepository.findByIdempotencyKeyForUpdate(idempotencyKey);
    }
}