package com.onatsubasi.finalcase.payment.application.port;

import com.onatsubasi.finalcase.payment.domain.model.Payment;
import com.onatsubasi.finalcase.payment.domain.model.PaymentCancellation;
import com.onatsubasi.finalcase.payment.domain.model.PaymentRefund;

public interface PaymentEventPublisher {

    void publishPaymentSucceeded(Payment payment);

    void publishPaymentFailed(Payment payment);

    void publishPaymentCancelled(Payment payment, PaymentCancellation cancellation);

    void publishPaymentRefunded(Payment payment, PaymentRefund refund);
}