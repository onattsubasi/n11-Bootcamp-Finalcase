package com.onatsubasi.finalcase.checkout.infrastructure.persistence;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataCheckoutSessionJpaRepository
        extends JpaRepository<CheckoutSession, UUID> {

    Optional<CheckoutSession> findByOrderId(UUID orderId);

    Optional<CheckoutSession> findByPaymentId(UUID paymentId);

    Page<CheckoutSession> findByUserId(UUID userId, Pageable pageable);

    Optional<CheckoutSession> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CheckoutSession c where c.id = :id and c.userId = :userId")
    Optional<CheckoutSession> findByIdAndUserIdForUpdate(UUID id, UUID userId);

    Optional<CheckoutSession> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CheckoutSession c where c.idempotencyKey = :idempotencyKey")
    Optional<CheckoutSession> findByIdempotencyKeyForUpdate(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CheckoutSession c where c.id = :id")
    Optional<CheckoutSession> findByIdForUpdate(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CheckoutSession c where c.orderId = :orderId")
    Optional<CheckoutSession> findByOrderIdForUpdate(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CheckoutSession c where c.paymentId = :paymentId")
    Optional<CheckoutSession> findByPaymentIdForUpdate(UUID paymentId);

    @Query("select c from CheckoutSession c where c.status = 'STARTED' and c.expiresAt < :now")
    List<CheckoutSession> findExpirableSessions(Instant now, Pageable pageable);

    Page<CheckoutSession> findByStatus(CheckoutStatus status, Pageable pageable);
}
