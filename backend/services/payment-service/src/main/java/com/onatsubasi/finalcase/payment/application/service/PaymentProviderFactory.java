package com.onatsubasi.finalcase.payment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.application.port.PaymentProviderPort;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProviderFactory {

    private final List<PaymentProviderPort> providers;

    public PaymentProviderPort getProvider(PaymentProviderCode providerCode) {
        return providers.stream()
                .filter(provider -> provider.providerCode() == providerCode)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn(
                            "event=payment.provider_not_supported provider={}",
                            providerCode);

                    return new BaseException(
                            PaymentErrorCode.PAYMENT_PROVIDER_NOT_SUPPORTED);
                });
    }
}