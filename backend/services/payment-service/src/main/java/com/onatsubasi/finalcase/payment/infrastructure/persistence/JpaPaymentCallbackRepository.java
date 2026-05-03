package com.onatsubasi.finalcase.payment.infrastructure.persistence;

import com.onatsubasi.finalcase.payment.domain.model.PaymentCallback;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentCallbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPaymentCallbackRepository implements PaymentCallbackRepository {

    private final SpringDataPaymentCallbackJpaRepository springDataRepository;

    @Override
    public PaymentCallback save(PaymentCallback callback) {
        return springDataRepository.save(callback);
    }

    @Override
    public Optional<PaymentCallback> findByProviderAndEventKey(
            PaymentProviderCode provider,
            String eventKey
    ) {
        return springDataRepository.findByProviderAndEventKey(provider, eventKey);
    }

    @Override
    public Optional<PaymentCallback> findByProviderAndEventKeyForUpdate(
            PaymentProviderCode provider,
            String eventKey
    ) {
        return springDataRepository.findByProviderAndEventKeyForUpdate(
                provider,
                eventKey
        );
    }
}