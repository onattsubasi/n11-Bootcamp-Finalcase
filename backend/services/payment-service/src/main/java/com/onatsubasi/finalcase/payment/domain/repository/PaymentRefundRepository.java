package com.onatsubasi.finalcase.payment.domain.repository;

import com.onatsubasi.finalcase.payment.domain.entity.PaymentRefund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRefundRepository {

    PaymentRefund save(PaymentRefund refund);

    Optional<PaymentRefund> findById(UUID id);

    Optional<PaymentRefund> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentRefund> findByIdempotencyKeyForUpdate(String idempotencyKey);

    Page<PaymentRefund> findByPaymentId(UUID paymentId, Pageable pageable);
}
