package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.model.PaymentRefund;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPaymentRefundJpaRepository
        extends JpaRepository<PaymentRefund, UUID> {

    Optional<PaymentRefund> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from PaymentRefund r where r.idempotencyKey = :idempotencyKey")
    Optional<PaymentRefund> findByIdempotencyKeyForUpdate(String idempotencyKey);

    Page<PaymentRefund> findByPayment_Id(UUID paymentId, Pageable pageable);
}
