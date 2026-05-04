package com.onatsubasi.finalcase.checkout.domain.repository;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckoutSessionRepository {

    CheckoutSession save(CheckoutSession checkoutSession);

    Optional<CheckoutSession> findById(UUID id);

    Optional<CheckoutSession> findByIdForUpdate(UUID id);

    Optional<CheckoutSession> findByIdAndUserId(UUID id, UUID userId);

    Optional<CheckoutSession> findByIdAndUserIdForUpdate(UUID id, UUID userId);

    Optional<CheckoutSession> findByOrderId(UUID orderId);

    Optional<CheckoutSession> findByOrderIdForUpdate(UUID orderId);

    Optional<CheckoutSession> findByPaymentId(UUID paymentId);

    Optional<CheckoutSession> findByPaymentIdForUpdate(UUID paymentId);

    Optional<CheckoutSession> findByIdempotencyKey(String idempotencyKey);

    Optional<CheckoutSession> findByIdempotencyKeyForUpdate(String idempotencyKey);

    Page<CheckoutSession> findByUserId(UUID userId, Pageable pageable);

    Page<CheckoutSession> findByStatus(CheckoutStatus status, Pageable pageable);

    Page<CheckoutSession> findAll(Pageable pageable);

    List<CheckoutSession> findExpirableSessions(Instant now, int limit);
}