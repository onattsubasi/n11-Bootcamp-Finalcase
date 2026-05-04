package com.onatsubasi.finalcase.payment.application.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentInitializeResponse;
import com.onatsubasi.finalcase.payment.domain.entity.Payment;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentAttempt;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentIdempotencyRecordRepository;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import com.onatsubasi.finalcase.payment.domain.entity.PaymentIdempotencyRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentIdempotencyService {

        private final PaymentIdempotencyRecordRepository idempotencyRepository;
        private final ObjectMapper objectMapper;

        @Transactional
        public PaymentIdempotencyRecord getOrCreateForUpdate(
                        String idempotencyKey,
                        String requestHash) {
                if (idempotencyKey == null || idempotencyKey.isBlank()) {
                        throw new BaseException(PaymentErrorCode.PAYMENT_IDEMPOTENCY_KEY_REQUIRED);
                }

                return idempotencyRepository
                                .findByIdempotencyKeyForUpdate(idempotencyKey.trim())
                                .map(existing -> {
                                        existing.validateSameRequest(requestHash);

                                        log.info(
                                                        "event=payment.idempotency_existing idempotencyKeyPresent=true paymentId={}",
                                                        existing.getPaymentId());

                                        return existing;
                                })
                                .orElseGet(() -> idempotencyRepository.save(
                                                new PaymentIdempotencyRecord(
                                                                idempotencyKey.trim(),
                                                                requestHash,
                                                                Instant.now().plusSeconds(300))));
        }

        public Optional<PaymentInitializeResponse> getStoredInitializeResponse(
                        PaymentIdempotencyRecord record) {
                if (record == null || !record.hasStoredResponse()) {
                        return Optional.empty();
                }

                return Optional.of(objectMapper.convertValue(
                                record.getResponsePayload(),
                                PaymentInitializeResponse.class));
        }

        @Transactional
        public void storeInitializeResponse(
                        PaymentIdempotencyRecord record,
                        Payment payment,
                        PaymentAttempt attempt,
                        PaymentInitializeResponse response) {
                record.attachPayment(payment.getId(), attempt.getId());
                record.storeResponse(
                                202,
                                objectMapper.convertValue(
                                                response,
                                                new TypeReference<Map<String, Object>>() {
                                                }));

                idempotencyRepository.save(record);
        }
}