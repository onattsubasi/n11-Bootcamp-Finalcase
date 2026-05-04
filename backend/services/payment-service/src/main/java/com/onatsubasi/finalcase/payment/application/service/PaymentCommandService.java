package com.onatsubasi.finalcase.payment.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.application.dto.request.InitializePaymentRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentInitializeResponse;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import com.onatsubasi.finalcase.payment.infrastructure.mapper.PaymentMapper;
import com.onatsubasi.finalcase.payment.domain.entity.Payment;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentIdempotencyRecord;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentAttemptRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRepository;
import com.onatsubasi.finalcase.payment.application.service.strategy.PaymentMethodStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandService {

        private final PaymentRepository paymentRepository;
        private final PaymentAttemptRepository paymentAttemptRepository;
        private final PaymentRequestHashService requestHashService;
        private final PaymentIdempotencyService idempotencyService;
        private final PaymentMethodStrategyFactory strategyFactory;
        private final PaymentMapper paymentMapper;

        @Transactional
        public PaymentInitializeResponse initializePayment(
                        InitializePaymentRequest request,
                        String idempotencyKey) {
                log.info(
                                "event=payment.initialize_requested checkoutId={} orderId={} userId={} provider={} method={} amount={} currency={}",
                                request.checkoutId(),
                                request.orderId(),
                                request.userId(),
                                request.provider(),
                                request.method(),
                                request.amount(),
                                request.currency());

                String requestHash = requestHashService.hash(request);

                PaymentIdempotencyRecord idempotencyRecord = idempotencyService.getOrCreateForUpdate(
                                idempotencyKey,
                                requestHash);

                Optional<PaymentInitializeResponse> storedResponse = idempotencyService
                                .getStoredInitializeResponse(idempotencyRecord);

                if (storedResponse.isPresent()) {
                        log.info(
                                        "event=payment.idempotent_replay orderId={} paymentId={}",
                                        request.orderId(),
                                        storedResponse.get().paymentId());

                        return storedResponse.get();
                }

                Payment payment = paymentRepository.findByOrderIdForUpdate(request.orderId())
                                .orElseGet(() -> paymentRepository.save(paymentMapper.toPayment(request)));

                if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                        throw new BaseException(PaymentErrorCode.PAYMENT_ALREADY_SUCCEEDED);
                }

                int nextAttemptNumber = paymentAttemptRepository.countByPaymentId(payment.getId()) + 1;

                PaymentAttempt attempt = paymentMapper.toPaymentAttempt(
                                request,
                                idempotencyKey,
                                requestHash,
                                nextAttemptNumber);

                payment.addAttempt(attempt);
                payment = paymentRepository.save(payment);

                PaymentMethodStrategy strategy = strategyFactory.getStrategy(request.method());

                PaymentInitializeResponse response = strategy.initialize(
                                payment,
                                attempt,
                                request);

                Payment saved = paymentRepository.save(payment);

                idempotencyService.storeInitializeResponse(
                                idempotencyRecord,
                                saved,
                                attempt,
                                response);

                log.info(
                                "event=payment.initialize_completed paymentId={} attemptId={} orderId={} status={} attemptStatus={}",
                                saved.getId(),
                                attempt.getId(),
                                saved.getOrderId(),
                                saved.getStatus(),
                                attempt.getStatus());

                return response;
        }
}