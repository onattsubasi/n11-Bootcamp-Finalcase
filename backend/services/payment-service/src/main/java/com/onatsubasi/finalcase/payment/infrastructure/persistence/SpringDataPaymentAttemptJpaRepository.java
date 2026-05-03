package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.model.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPaymentAttemptJpaRepository extends JpaRepository<PaymentAttempt, UUID> {

    Optional<PaymentAttempt> findByProviderAndProviderToken(
            PaymentProviderCode provider,
            String providerToken
    );

    Optional<PaymentAttempt> findByIdempotencyKey(String idempotencyKey);

    int countByPayment_Id(UUID paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from PaymentAttempt a where a.idempotencyKey = :idempotencyKey")
    Optional<PaymentAttempt> findByIdempotencyKeyForUpdate(String idempotencyKey);
}
