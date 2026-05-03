package com.onatsubasi.finalcase.checkout.application.port;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;

public interface CheckoutEventPublisher {

    void publishCheckoutSubmitted(CheckoutSession session);

    void publishCheckoutPaymentPending(CheckoutSession session);

    void publishCheckoutCompleted(CheckoutSession session);

    void publishCheckoutFailed(CheckoutSession session);

    void publishCheckoutCompensated(CheckoutSession session);

    void publishCheckoutFinalizationFailed(CheckoutSession session);

    void publishCheckoutCompensationFailed(CheckoutSession session);

    void publishCheckoutCancelled(CheckoutSession session);
}