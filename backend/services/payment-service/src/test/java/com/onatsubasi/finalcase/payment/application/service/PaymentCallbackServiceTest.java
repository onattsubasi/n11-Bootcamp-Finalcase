package com.onatsubasi.finalcase.payment.application.service;

import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderPaymentRetrieveCommand;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderPaymentRetrieveResult;
import com.onatsubasi.finalcase.payment.application.dto.request.IyzicoCheckoutFormCallbackRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentDetailResponse;
import com.onatsubasi.finalcase.payment.application.port.PaymentEventPublisher;
import com.onatsubasi.finalcase.payment.application.port.PaymentProviderPort;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentAttemptStatus;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import com.onatsubasi.finalcase.payment.domain.model.Payment;
import com.onatsubasi.finalcase.payment.domain.model.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.model.PaymentCallback;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentAttemptRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentCallbackRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRepository;
import com.onatsubasi.finalcase.payment.infrastructure.mapper.PaymentMapper;
import com.onatsubasi.finalcase.payment.support.PaymentTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCallbackServiceTest {

    @Mock
    private PaymentCallbackRepository callbackRepository;
    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentProviderFactory providerFactory;
    @Mock
    private PaymentEventPublisher eventPublisher;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentProviderPort provider;

    @InjectMocks
    private PaymentCallbackService service;

    @Test
    void handleIyzicoCallbackRetrievesProviderResultMarksPaymentSucceededAndPublishesEvent() {
        IyzicoCheckoutFormCallbackRequest request = new IyzicoCheckoutFormCallbackRequest("token-1", "success", "conversation-1");
        Payment payment = PaymentTestData.paymentWaitingProviderAction();
        PaymentAttempt attempt = attempt();
        payment.addAttempt(attempt);
        PaymentCallback callback = new PaymentCallback(PaymentProviderCode.IYZICO, "token-1", "token-1", Map.of("status", "success"));
        PaymentDetailResponse detailResponse = detailResponse(PaymentStatus.SUCCEEDED);

        when(callbackRepository.findByProviderAndEventKeyForUpdate(PaymentProviderCode.IYZICO, "token-1")).thenReturn(Optional.empty());
        when(callbackRepository.save(any(PaymentCallback.class))).thenReturn(callback);
        when(paymentAttemptRepository.findByProviderAndProviderToken(PaymentProviderCode.IYZICO, "token-1")).thenReturn(Optional.of(attempt));
        when(paymentRepository.findByIdForUpdate(payment.getId())).thenReturn(Optional.of(payment));
        when(providerFactory.getProvider(PaymentProviderCode.IYZICO)).thenReturn(provider);
        when(paymentMapper.toProviderRetrieveCommand(attempt)).thenReturn(ProviderPaymentRetrieveCommand.builder().providerToken("token-1").build());
        when(provider.retrievePayment(any())).thenReturn(ProviderPaymentRetrieveResult.builder()
                .success(true)
                .paymentStatus(PaymentStatus.SUCCEEDED)
                .attemptStatus(PaymentAttemptStatus.SUCCEEDED)
                .providerPaymentId("provider-payment-1")
                .providerTransactionId("provider-tx-1")
                .providerConversationId("conversation-1")
                .providerStatus("SUCCESS")
                .paidAmount(new BigDecimal("100.00"))
                .currency("TRY")
                .providerResponseSnapshot(Map.of())
                .build());
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDetailResponse(payment)).thenReturn(detailResponse);

        PaymentDetailResponse actual = service.handleIyzicoCheckoutFormCallback(request);

        assertThat(actual.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.SUCCEEDED);
        verify(eventPublisher).publishPaymentSucceeded(payment);
    }

    @Test
    void handleIyzicoCallbackReturnsExistingPaymentWhenCallbackAlreadyProcessed() {
        IyzicoCheckoutFormCallbackRequest request = new IyzicoCheckoutFormCallbackRequest("token-1", "success", null);
        Payment payment = PaymentTestData.succeededPayment();
        PaymentAttempt attempt = attempt();
        payment.addAttempt(attempt);
        PaymentCallback callback = new PaymentCallback(PaymentProviderCode.IYZICO, "token-1", "token-1", Map.of());
        callback.markProcessed();
        PaymentDetailResponse detailResponse = detailResponse(PaymentStatus.SUCCEEDED);

        when(callbackRepository.findByProviderAndEventKeyForUpdate(PaymentProviderCode.IYZICO, "token-1")).thenReturn(Optional.of(callback));
        when(paymentAttemptRepository.findByProviderAndProviderToken(PaymentProviderCode.IYZICO, "token-1")).thenReturn(Optional.of(attempt));
        when(paymentMapper.toDetailResponse(payment)).thenReturn(detailResponse);

        PaymentDetailResponse actual = service.handleIyzicoCheckoutFormCallback(request);

        assertThat(actual).isSameAs(detailResponse);
        verifyNoInteractions(providerFactory, provider, eventPublisher, paymentRepository);
    }

    private PaymentAttempt attempt() {
        PaymentAttempt attempt = new PaymentAttempt(
                1,
                "idem-1",
                "hash-1",
                PaymentProviderCode.IYZICO,
                PaymentMethod.CHECKOUT_FORM,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "TRY");
        attempt.markInitialized("token-1", "conversation-1", "https://pay.example.com", null, Map.of());
        attempt.markWaitingProviderAction();
        return attempt;
    }

    private PaymentDetailResponse detailResponse(PaymentStatus status) {
        return new PaymentDetailResponse(
                PaymentTestData.PAYMENT_ID,
                PaymentTestData.CHECKOUT_ID,
                PaymentTestData.ORDER_ID,
                "ORD-20260501-000001",
                PaymentTestData.USER_ID,
                PaymentProviderCode.IYZICO,
                PaymentMethod.CHECKOUT_FORM,
                status,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                "TRY",
                "provider-payment-1",
                "provider-tx-1",
                "conversation-1",
                "SUCCESS",
                null,
                java.util.List.of(),
                null,
                null,
                null);
    }
}
