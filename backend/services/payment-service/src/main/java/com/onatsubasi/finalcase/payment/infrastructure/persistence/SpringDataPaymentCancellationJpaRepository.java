package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.entity.PaymentCancellation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPaymentCancellationJpaRepository
        extends JpaRepository<PaymentCancellation, UUID> {

    Optional<PaymentCancellation> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PaymentCancellation c where c.idempotencyKey = :idempotencyKey")
    Optional<PaymentCancellation> findByIdempotencyKeyForUpdate(String idempotencyKey);
}
