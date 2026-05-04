package com.onatsubasi.finalcase.payment.application.port;

import com.onatsubasi.finalcase.payment.domain.entity.Payment;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentCancellation;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentRefund;

public interface PaymentEventPublisher {

    void publishPaymentSucceeded(Payment payment);

    void publishPaymentFailed(Payment payment);

    void publishPaymentCancelled(Payment payment, PaymentCancellation cancellation);

    void publishPaymentRefunded(Payment payment, PaymentRefund refund);
}