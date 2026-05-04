package com.onatsubasi.finalcase.payment.domain.repository;

import com.onatsubasi.finalcase.payment.domain.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    Optional<Payment> findByIdForUpdate(UUID id);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByOrderIdForUpdate(UUID orderId);

    Optional<Payment> findByCheckoutId(UUID checkoutId);

    Optional<Payment> findByCheckoutIdForUpdate(UUID checkoutId);

    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    Page<Payment> findAll(Pageable pageable);
}