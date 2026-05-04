package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.entity.PaymentRefund;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaPaymentRefundRepository implements PaymentRefundRepository {

    private final SpringDataPaymentRefundJpaRepository springDataRepository;

    @Override
    public PaymentRefund save(PaymentRefund refund) {
        return springDataRepository.save(refund);
    }

    @Override
    public Optional<PaymentRefund> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<PaymentRefund> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<PaymentRefund> findByIdempotencyKeyForUpdate(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKeyForUpdate(idempotencyKey);
    }

    @Override
    public Page<PaymentRefund> findByPaymentId(UUID paymentId, Pageable pageable) {
        return springDataRepository.findByPayment_Id(paymentId, pageable);
    }
}
