package com.onatsubasi.finalcase.checkout.application.service;

import java.util.UUID;

public interface CheckoutRetryService {
    void retryFinalization(UUID checkoutId);
    void retryCompensation(UUID checkoutId);
}
