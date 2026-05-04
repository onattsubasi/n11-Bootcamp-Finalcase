package com.onatsubasi.finalcase.payment.domain;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import com.onatsubasi.finalcase.payment.domain.entity.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentLifecycleTest {

    @Test
    void constructorNormalizesCurrencyAndStartsAsInitiated() {
        Payment payment = newPayment(new BigDecimal("120.50"));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(payment.getCurrency()).isEqualTo("TRY");
        assertThat(payment.getRefundedAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void successFinalizesPaymentAndIsIdempotentForSameTerminalState() {
        Payment payment = newPayment(new BigDecimal("100.00"));
        payment.markWaitingProviderAction();

        payment.markSucceeded("payment-1", "tx-1", "conversation-1", "SUCCESS");
        payment.markSucceeded("payment-1", "tx-1", "conversation-1", "SUCCESS");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(payment.getProviderPaymentId()).isEqualTo("payment-1");
        assertThat(payment.getFailureReason()).isNull();
    }

    @Test
    void refundCannotExceedPaidAmount() {
        Payment payment = newPayment(new BigDecimal("100.00"));
        payment.markSucceeded("payment-1", "tx-1", "conversation-1", "SUCCESS");

        payment.applyRefund(new BigDecimal("40.00"));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
        assertThatThrownBy(() -> payment.applyRefund(new BigDecimal("70.00")))
                .isInstanceOf(BaseException.class);
    }

    private Payment newPayment(BigDecimal amount) {
        return new Payment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-20260501-000001",
                UUID.randomUUID(),
                PaymentProviderCode.IYZICO,
                PaymentMethod.CHECKOUT_FORM,
                amount,
                amount,
                "try"
        );
    }
}
