package com.onatsubasi.finalcase.payment.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.onatsubasi.finalcase.payment.application.service.strategy.PaymentMethodStrategy;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodStrategyFactory {

    private final List<PaymentMethodStrategy> strategies;

    public PaymentMethodStrategy getStrategy(PaymentMethod method) {
        return strategies.stream()
                .filter(strategy -> strategy.method() == method)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn(
                            "event=payment.method_not_supported method={}",
                            method);

                    return new BaseException(
                            PaymentErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
                });
    }
}