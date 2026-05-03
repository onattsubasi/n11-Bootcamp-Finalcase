package com.onatsubasi.finalcase.order.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
public class OrderPaymentSummary {

    @Getter
    @Column(name = "payment_id")
    private UUID paymentId;

    @Getter
    @Column(name = "payment_provider", length = 50)
    private String paymentProvider;

    @Getter
    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Getter
    @Column(name = "provider_transaction_id", length = 150)
    private String providerTransactionId;

    protected OrderPaymentSummary() {
    }

    public static OrderPaymentSummary empty() {
        return new OrderPaymentSummary(null, null, null, null);
    }

    public OrderPaymentSummary(
            UUID paymentId,
            String paymentProvider,
            String paymentStatus,
            String providerTransactionId
    ) {
        this.paymentId = paymentId;
        this.paymentProvider = normalize(paymentProvider);
        this.paymentStatus = normalize(paymentStatus);
        this.providerTransactionId = normalize(providerTransactionId);
    }

    public void update(
            UUID paymentId,
            String paymentProvider,
            String paymentStatus,
            String providerTransactionId
    ) {
        this.paymentId = paymentId;
        this.paymentProvider = normalize(paymentProvider);
        this.paymentStatus = normalize(paymentStatus);
        this.providerTransactionId = normalize(providerTransactionId);
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
