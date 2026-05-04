package com.onatsubasi.finalcase.payment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderCancelCommand;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderCancelResult;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderRefundCommand;
import com.onatsubasi.finalcase.payment.application.dto.provider.ProviderRefundResult;
import com.onatsubasi.finalcase.payment.application.dto.request.CancelPaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.request.RefundPaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentCancellationResponse;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentRefundResponse;
import com.onatsubasi.finalcase.payment.application.port.PaymentEventPublisher;
import com.onatsubasi.finalcase.payment.application.port.PaymentProviderPort;
import com.onatsubasi.finalcase.payment.domain.enums.CancellationStatus;
import com.onatsubasi.finalcase.payment.domain.enums.RefundStatus;
import com.onatsubasi.finalcase.payment.domain.entity.Payment;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentCancellation;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentRefund;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentCancellationRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRefundRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRefundServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentRefundRepository refundRepository;
    @Mock
    private PaymentCancellationRepository cancellationRepository;
    @Mock
    private PaymentProviderFactory providerFactory;
    @Mock
    private PaymentEventPublisher eventPublisher;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentRequestHashService requestHashService;
    @Mock
    private PaymentProviderPort provider;

    @InjectMocks
    private PaymentRefundService service;

    @Test
    void refundPaymentReplaysExistingRefundWhenPayloadMatches() {
        RefundPaymentRequest request = new RefundPaymentRequest(new BigDecimal("20.00"), "TRY", "customer request");
        PaymentRefund existingRefund = new PaymentRefund(
                PaymentTestData.succeededPayment(),
                "refund-key",
                "hash-1",
                request.amount(),
                request.currency());
        PaymentRefundResponse response = new PaymentRefundResponse(null, PaymentTestData.PAYMENT_ID, request.amount(), "TRY", RefundStatus.REQUESTED, null, null, null, null, null);

        when(requestHashService.hash(any())).thenReturn("hash-1");
        when(refundRepository.findByIdempotencyKeyForUpdate("refund-key")).thenReturn(Optional.of(existingRefund));
        when(paymentMapper.toRefundResponse(existingRefund)).thenReturn(response);

        PaymentRefundResponse actual = service.refundPayment(PaymentTestData.PAYMENT_ID, "refund-key", request);

        assertThat(actual).isSameAs(response);
        verifyNoInteractions(paymentRepository, providerFactory, provider, eventPublisher);
    }

    @Test
    void refundPaymentRejectsSameKeyWithDifferentPayload() {
        RefundPaymentRequest request = new RefundPaymentRequest(new BigDecimal("20.00"), "TRY", "customer request");
        PaymentRefund existingRefund = new PaymentRefund(
                PaymentTestData.succeededPayment(),
                "refund-key",
                "old-hash",
                request.amount(),
                request.currency());

        when(requestHashService.hash(any())).thenReturn("new-hash");
        when(refundRepository.findByIdempotencyKeyForUpdate("refund-key")).thenReturn(Optional.of(existingRefund));

        assertThatThrownBy(() -> service.refundPayment(PaymentTestData.PAYMENT_ID, "refund-key", request))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void refundPaymentCallsProviderUpdatesPaymentAndPublishesEventOnSuccess() {
        RefundPaymentRequest request = new RefundPaymentRequest(new BigDecimal("20.00"), "TRY", "customer request");
        Payment payment = PaymentTestData.succeededPayment();
        PaymentRefundResponse response = new PaymentRefundResponse(null, PaymentTestData.PAYMENT_ID, request.amount(), "TRY", RefundStatus.SUCCEEDED, "refund-1", "success", null, null, null);

        when(requestHashService.hash(any())).thenReturn("hash-1");
        when(refundRepository.findByIdempotencyKeyForUpdate("refund-key")).thenReturn(Optional.empty());
        when(paymentRepository.findByIdForUpdate(PaymentTestData.PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(PaymentRefund.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(providerFactory.getProvider(payment.getProvider())).thenReturn(provider);
        when(paymentMapper.toProviderRefundCommand(any(), any(), any())).thenReturn(ProviderRefundCommand.builder().paymentId(PaymentTestData.PAYMENT_ID).amount(request.amount()).currency("TRY").build());
        when(provider.refundPayment(any())).thenReturn(ProviderRefundResult.builder().success(true).providerRefundId("refund-1").providerStatus("success").providerResponseSnapshot(Map.of()).build());
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toRefundResponse(any(PaymentRefund.class))).thenReturn(response);

        PaymentRefundResponse actual = service.refundPayment(PaymentTestData.PAYMENT_ID, "refund-key", request);

        assertThat(actual.status()).isEqualTo(RefundStatus.SUCCEEDED);
        assertThat(payment.getRefundedAmount()).isEqualByComparingTo("20.00");
        verify(eventPublisher).publishPaymentRefunded(eq(payment), any(PaymentRefund.class));
    }

    @Test
    void cancelPaymentReplaysExistingCancellationWhenPayloadMatches() {
        CancelPaymentRequest request = new CancelPaymentRequest("duplicate call");
        PaymentCancellation existingCancellation = new PaymentCancellation(
                PaymentTestData.succeededPayment(),
                "cancel-key",
                "hash-1");
        PaymentCancellationResponse response = new PaymentCancellationResponse(null, PaymentTestData.PAYMENT_ID, CancellationStatus.REQUESTED, null, null, null, null, null);

        when(requestHashService.hash(any())).thenReturn("hash-1");
        when(cancellationRepository.findByIdempotencyKeyForUpdate("cancel-key")).thenReturn(Optional.of(existingCancellation));
        when(paymentMapper.toCancellationResponse(existingCancellation)).thenReturn(response);

        PaymentCancellationResponse actual = service.cancelPayment(PaymentTestData.PAYMENT_ID, "cancel-key", request);

        assertThat(actual).isSameAs(response);
        verifyNoInteractions(paymentRepository, providerFactory, provider, eventPublisher);
    }

    @Test
    void cancelPaymentCallsProviderAndPublishesEventOnSuccess() {
        CancelPaymentRequest request = new CancelPaymentRequest("admin cancel");
        Payment payment = PaymentTestData.succeededPayment();
        PaymentCancellationResponse response = new PaymentCancellationResponse(null, PaymentTestData.PAYMENT_ID, CancellationStatus.SUCCEEDED, "cancel-1", "success", null, null, Instant.now());

        when(requestHashService.hash(any())).thenReturn("hash-1");
        when(cancellationRepository.findByIdempotencyKeyForUpdate("cancel-key")).thenReturn(Optional.empty());
        when(paymentRepository.findByIdForUpdate(PaymentTestData.PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(cancellationRepository.save(any(PaymentCancellation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(providerFactory.getProvider(payment.getProvider())).thenReturn(provider);
        when(paymentMapper.toProviderCancelCommand(any(), any(), any())).thenReturn(ProviderCancelCommand.builder().paymentId(PaymentTestData.PAYMENT_ID).build());
        when(provider.cancelPayment(any())).thenReturn(ProviderCancelResult.builder().success(true).providerCancelId("cancel-1").providerStatus("success").providerResponseSnapshot(Map.of()).build());
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toCancellationResponse(any(PaymentCancellation.class))).thenReturn(response);

        PaymentCancellationResponse actual = service.cancelPayment(PaymentTestData.PAYMENT_ID, "cancel-key", request);

        assertThat(actual.status()).isEqualTo(CancellationStatus.SUCCEEDED);
        verify(eventPublisher).publishPaymentCancelled(eq(payment), any(PaymentCancellation.class));
    }
}
