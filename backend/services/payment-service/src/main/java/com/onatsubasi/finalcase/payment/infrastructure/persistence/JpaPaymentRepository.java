package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.model.Payment;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaPaymentRepository implements PaymentRepository {

    private final SpringDataPaymentJpaRepository springDataRepository;

    @Override
    public Payment save(Payment payment) {
        return springDataRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByIdForUpdate(UUID id) {
        return springDataRepository.findByIdForUpdate(id);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return springDataRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<Payment> findByOrderIdForUpdate(UUID orderId) {
        return springDataRepository.findByOrderIdForUpdate(orderId);
    }

    @Override
    public Optional<Payment> findByCheckoutId(UUID checkoutId) {
        return springDataRepository.findByCheckoutId(checkoutId);
    }

    @Override
    public Optional<Payment> findByCheckoutIdForUpdate(UUID checkoutId) {
        return springDataRepository.findByCheckoutIdForUpdate(checkoutId);
    }

    @Override
    public Page<Payment> findByUserId(UUID userId, Pageable pageable) {
        return springDataRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Payment> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }
}