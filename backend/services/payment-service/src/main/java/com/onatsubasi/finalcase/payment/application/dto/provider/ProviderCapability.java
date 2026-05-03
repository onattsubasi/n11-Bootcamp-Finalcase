package com.onatsubasi.finalcase.payment.application.dto.provider;

import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import lombok.Builder;

import java.util.Set;

@Builder
public record ProviderCapability(
        PaymentProviderCode provider,
        Set<PaymentMethod> supportedMethods,
        boolean supportsRefund,
        boolean supportsCancel,
        boolean supportsInstallments,
        boolean requiresRedirect,
        boolean requiresCallback
) {
    public boolean supports(PaymentMethod method) {
        return supportedMethods != null && supportedMethods.contains(method);
    }
}