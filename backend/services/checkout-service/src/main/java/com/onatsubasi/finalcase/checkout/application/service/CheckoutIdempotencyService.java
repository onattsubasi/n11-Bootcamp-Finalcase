package com.onatsubasi.finalcase.checkout.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSubmitResponse;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutIdempotencyRecord;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.checkout.domain.repository.CheckoutIdempotencyRecordRepository;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutIdempotencyService {

    private final CheckoutIdempotencyRecordRepository idempotencyRepository;
    private final ObjectMapper objectMapper;


    @Transactional
    public CheckoutIdempotencyRecord getOrCreateForUpdate(
            UUID userId,
            String idempotencyKey,
            String requestHash
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BaseException(
                    CheckoutErrorCode.CHECKOUT_IDEMPOTENCY_KEY_REQUIRED
            );
        }

        return idempotencyRepository
                .findByIdempotencyKeyForUpdate(idempotencyKey.trim())
                .map(existing -> {
                    existing.validateSameRequest(requestHash);

                    if (!existing.getUserId().equals(userId)) {
                        throw new BaseException(
                                CheckoutErrorCode.CHECKOUT_IDEMPOTENCY_CONFLICT
                        );
                    }

                    return existing;
                })
                .orElseGet(() -> idempotencyRepository.save(
                        new CheckoutIdempotencyRecord(
                                idempotencyKey.trim(),
                                userId,
                                requestHash,
                                Instant.now().plusSeconds(300)
                        )
                ));
    }

    public Optional<CheckoutSubmitResponse> getStoredSubmitResponse(
            CheckoutIdempotencyRecord record
    ) {
        if (record == null || !record.hasStoredResponse()) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.convertValue(
                record.getResponsePayload(),
                CheckoutSubmitResponse.class
        ));
    }

    @Transactional
    public void storeSubmitResponse(
            CheckoutIdempotencyRecord record,
            UUID checkoutSessionId,
            CheckoutSubmitResponse response
    ) {
        record.attachCheckout(checkoutSessionId);
        record.storeResponse(
                202,
                objectMapper.convertValue(
                        response,
                        new TypeReference<Map<String, Object>>() {
                        }
                )
        );

        idempotencyRepository.save(record);
    }
}