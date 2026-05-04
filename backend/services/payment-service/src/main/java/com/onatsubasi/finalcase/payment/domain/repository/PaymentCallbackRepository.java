package com.onatsubasi.finalcase.payment.domain.repository;

import com.onatsubasi.finalcase.payment.domain.entity.PaymentCallback;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;

import java.util.Optional;

public interface PaymentCallbackRepository {

    PaymentCallback save(PaymentCallback callback);

    Optional<PaymentCallback> findByProviderAndEventKey(
            PaymentProviderCode provider,
            String eventKey
    );

    Optional<PaymentCallback> findByProviderAndEventKeyForUpdate(
            PaymentProviderCode provider,
            String eventKey
    );
}