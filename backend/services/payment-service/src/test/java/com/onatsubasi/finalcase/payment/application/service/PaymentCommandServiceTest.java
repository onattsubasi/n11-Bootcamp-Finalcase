package com.onatsubasi.finalcase.payment.application.service;

import com.onatsubasi.finalcase.payment.application.dto.request.InitializePaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentInitializeResponse;
import com.onatsubasi.finalcase.payment.application.service.strategy.PaymentMethodStrategy;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentAttemptStatus;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import com.onatsubasi.finalcase.payment.domain.model.Payment;
import com.onatsubasi.finalcase.payment.domain.model.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.model.PaymentIdempotencyRecord;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentAttemptRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRepository;
import com.onatsubasi.finalcase.payment.infrastructure.mapper.PaymentMapper;
import com.onatsubasi.finalcase.payment.support.PaymentTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCommandServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;
    @Mock
    private PaymentRequestHashService requestHashService;
    @Mock
    private PaymentIdempotencyService idempotencyService;
    @Mock
    private PaymentMethodStrategyFactory strategyFactory;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentMethodStrategy strategy;

    @InjectMocks
    private PaymentCommandService service;

    @Test
    void initializePaymentReturnsStoredResponseWhenIdempotencyRecordAlreadyHasResponse() {
        InitializePaymentRequest request = PaymentTestData.initializePaymentRequest();
        PaymentIdempotencyRecord record = new PaymentIdempotencyRecord("idem-1", "hash-1", Instant.now());
        PaymentInitializeResponse stored = response(PaymentStatus.WAITING_PROVIDER_ACTION);

        when(requestHashService.hash(request)).thenReturn("hash-1");
        when(idempotencyService.getOrCreateForUpdate("idem-1", "hash-1")).thenReturn(record);
        when(idempotencyService.getStoredInitializeResponse(record)).thenReturn(Optional.of(stored));

        PaymentInitializeResponse actual = service.initializePayment(request, "idem-1");

        assertThat(actual).isSameAs(stored);
        verifyNoInteractions(paymentRepository, paymentAttemptRepository, strategyFactory, strategy, paymentMapper);
    }

    @Test
    void initializePaymentCreatesAttemptAndStoresIdempotentResponse() {
        InitializePaymentRequest request = PaymentTestData.initializePaymentRequest();
        PaymentIdempotencyRecord record = new PaymentIdempotencyRecord("idem-1", "hash-1", Instant.now());
        Payment payment = PaymentTestData.paymentWaitingProviderAction();
        PaymentAttempt attempt = new PaymentAttempt(
                1,
                "idem-1",
                "hash-1",
                PaymentProviderCode.IYZICO,
                PaymentMethod.CHECKOUT_FORM,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "TRY");
        PaymentInitializeResponse response = response(PaymentStatus.WAITING_PROVIDER_ACTION);

        when(requestHashService.hash(request)).thenReturn("hash-1");
        when(idempotencyService.getOrCreateForUpdate("idem-1", "hash-1")).thenReturn(record);
        when(idempotencyService.getStoredInitializeResponse(record)).thenReturn(Optional.empty());
        when(paymentRepository.findByOrderIdForUpdate(request.orderId())).thenReturn(Optional.empty());
        when(paymentMapper.toPayment(request)).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentAttemptRepository.countByPaymentId(payment.getId())).thenReturn(0);
        when(paymentMapper.toPaymentAttempt(request, "idem-1", "hash-1", 1)).thenReturn(attempt);
        when(strategyFactory.getStrategy(PaymentMethod.CHECKOUT_FORM)).thenReturn(strategy);
        when(strategy.initialize(payment, attempt, request)).thenReturn(response);

        PaymentInitializeResponse actual = service.initializePayment(request, "idem-1");

        assertThat(actual).isSameAs(response);
        verify(idempotencyService).storeInitializeResponse(record, payment, attempt, response);
    }

    private PaymentInitializeResponse response(PaymentStatus paymentStatus) {
        return new PaymentInitializeResponse(
                PaymentTestData.PAYMENT_ID,
                PaymentTestData.PAYMENT_ID,
                PaymentTestData.ORDER_ID,
                PaymentTestData.CHECKOUT_ID,
                PaymentProviderCode.IYZICO,
                PaymentMethod.CHECKOUT_FORM,
                paymentStatus,
                PaymentAttemptStatus.WAITING_PROVIDER_ACTION,
                "token-1",
                "https://pay.example.com",
                null,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "TRY");
    }
}
