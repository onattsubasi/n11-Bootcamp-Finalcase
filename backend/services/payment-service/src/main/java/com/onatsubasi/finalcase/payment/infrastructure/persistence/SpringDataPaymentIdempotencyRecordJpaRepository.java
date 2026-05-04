package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.entity.PaymentIdempotencyRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPaymentIdempotencyRecordJpaRepository
        extends JpaRepository<PaymentIdempotencyRecord, UUID> {

    Optional<PaymentIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r from PaymentIdempotencyRecord r
            where r.idempotencyKey = :idempotencyKey
           """)
    Optional<PaymentIdempotencyRecord> findByIdempotencyKeyForUpdate(
            String idempotencyKey
    );
}