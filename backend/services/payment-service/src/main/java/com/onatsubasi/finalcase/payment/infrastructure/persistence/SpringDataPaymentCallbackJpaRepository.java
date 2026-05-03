package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.model.PaymentCallback;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPaymentCallbackJpaRepository
        extends JpaRepository<PaymentCallback, UUID> {

    Optional<PaymentCallback> findByProviderAndEventKey(
            PaymentProviderCode provider,
            String eventKey
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select c from PaymentCallback c
            where c.provider = :provider
              and c.eventKey = :eventKey
           """)
    Optional<PaymentCallback> findByProviderAndEventKeyForUpdate(
            PaymentProviderCode provider,
            String eventKey
    );
}