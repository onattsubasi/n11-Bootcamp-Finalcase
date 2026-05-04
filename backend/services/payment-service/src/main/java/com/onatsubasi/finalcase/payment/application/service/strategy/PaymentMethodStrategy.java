package com.onatsubasi.finalcase.payment.application.service.strategy;

import com.onatsubasi.finalcase.payment.application.dto.request.InitializePaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentInitializeResponse;
import com.onatsubasi.finalcase.payment.domain.entity.Payment;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;

public interface PaymentMethodStrategy {

    PaymentMethod method();

    PaymentInitializeResponse initialize(
            Payment payment,
            PaymentAttempt attempt,
            InitializePaymentRequest request);
}