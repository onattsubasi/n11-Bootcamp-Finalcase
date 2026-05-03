package com.onatsubasi.finalcase.payment.application.port;

import com.onatsubasi.finalcase.payment.application.dto.provider.*;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;

public interface PaymentProviderPort {

        PaymentProviderCode providerCode();

        ProviderCapability capability();

        ProviderPaymentInitializeResult initializePayment(
                        ProviderPaymentInitializeCommand command);

        ProviderPaymentRetrieveResult retrievePayment(
                        ProviderPaymentRetrieveCommand command);

        ProviderRefundResult refundPayment(
                        ProviderRefundCommand command);

        ProviderCancelResult cancelPayment(
                        ProviderCancelCommand command);

        default boolean supports(PaymentMethod method) {
                return capability().supports(method);
        }
}