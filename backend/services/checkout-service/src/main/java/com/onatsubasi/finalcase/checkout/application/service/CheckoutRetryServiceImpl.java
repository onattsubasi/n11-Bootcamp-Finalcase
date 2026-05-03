package com.onatsubasi.finalcase.checkout.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutRetryServiceImpl implements CheckoutRetryService {

    private final CheckoutFinalizationService finalizationService;
    private final CheckoutCompensationService compensationService;

    @Override
    public void retryFinalization(UUID checkoutId) {
        finalizationService.retryFinalization(checkoutId);
    }

    @Override
    public void retryCompensation(UUID checkoutId) {
        compensationService.retryCompensation(checkoutId);
    }
}
