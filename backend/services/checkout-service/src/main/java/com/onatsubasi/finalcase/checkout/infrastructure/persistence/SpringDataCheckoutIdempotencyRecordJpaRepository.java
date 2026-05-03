package com.onatsubasi.finalcase.checkout.infrastructure.persistence;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutIdempotencyRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataCheckoutIdempotencyRecordJpaRepository
        extends JpaRepository<CheckoutIdempotencyRecord, UUID> {

    Optional<CheckoutIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r from CheckoutIdempotencyRecord r
            where r.idempotencyKey = :idempotencyKey
           """)
    Optional<CheckoutIdempotencyRecord> findByIdempotencyKeyForUpdate(
            String idempotencyKey
    );
}