package com.onatsubasi.finalcase.checkout.infrastructure.persistence;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;
import com.onatsubasi.finalcase.checkout.domain.repository.CheckoutSessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCheckoutSessionRepository implements CheckoutSessionRepository {

    private final SpringDataCheckoutSessionJpaRepository springDataRepository;

    public JpaCheckoutSessionRepository(
            SpringDataCheckoutSessionJpaRepository springDataRepository
    ) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public CheckoutSession save(CheckoutSession checkoutSession) {
        return springDataRepository.save(checkoutSession);
    }

    @Override
    public Optional<CheckoutSession> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<CheckoutSession> findByIdForUpdate(UUID id) {
        return springDataRepository.findByIdForUpdate(id);
    }

    @Override
    public Optional<CheckoutSession> findByIdAndUserId(UUID id, UUID userId) {
        return springDataRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public Optional<CheckoutSession> findByIdAndUserIdForUpdate(UUID id, UUID userId) {
        return springDataRepository.findByIdAndUserIdForUpdate(id, userId);
    }

    @Override
    public Optional<CheckoutSession> findByOrderId(UUID orderId) {
        return springDataRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<CheckoutSession> findByOrderIdForUpdate(UUID orderId) {
        return springDataRepository.findByOrderIdForUpdate(orderId);
    }

    @Override
    public Optional<CheckoutSession> findByPaymentId(UUID paymentId) {
        return springDataRepository.findByPaymentId(paymentId);
    }

    @Override
    public Optional<CheckoutSession> findByPaymentIdForUpdate(UUID paymentId) {
        return springDataRepository.findByPaymentIdForUpdate(paymentId);
    }

    @Override
    public Optional<CheckoutSession> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<CheckoutSession> findByIdempotencyKeyForUpdate(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKeyForUpdate(idempotencyKey);
    }

    @Override
    public Page<CheckoutSession> findByUserId(UUID userId, Pageable pageable) {
        return springDataRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<CheckoutSession> findByStatus(CheckoutStatus status, Pageable pageable) {
        return springDataRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<CheckoutSession> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }

    @Override
    public List<CheckoutSession> findExpirableSessions(Instant now, int limit) {
        return springDataRepository.findExpirableSessions(now, PageRequest.of(0, limit));
    }
}
