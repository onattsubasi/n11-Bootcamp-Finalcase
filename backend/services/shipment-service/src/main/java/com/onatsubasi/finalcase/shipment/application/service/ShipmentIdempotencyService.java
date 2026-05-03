package com.onatsubasi.finalcase.shipment.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentDetailResponse;
import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentIdempotencyRecord;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import com.onatsubasi.finalcase.shipment.domain.repository.ShipmentIdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentIdempotencyService {

    private final ShipmentIdempotencyRecordRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ShipmentIdempotencyRecord getOrCreateForUpdate(
            String idempotencyKey,
            String requestHash
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BaseException(ShipmentErrorCode.SHIPMENT_IDEMPOTENCY_KEY_REQUIRED);
        }

        return idempotencyRepository
                .findByIdempotencyKeyForUpdate(idempotencyKey.trim())
                .map(existing -> {
                    existing.validateSameRequest(requestHash);

                    log.info(
                            "event=shipment.idempotency_existing shipmentId={} orderId={}",
                            existing.getShipmentId(),
                            existing.getOrderId()
                    );

                    return existing;
                })
                .orElseGet(() -> idempotencyRepository.save(
                        new ShipmentIdempotencyRecord(
                                idempotencyKey.trim(),
                                requestHash,
                                Instant.now().plusSeconds(300)
                        )
                ));
    }

    public Optional<ShipmentDetailResponse> getStoredShipmentResponse(
            ShipmentIdempotencyRecord record
    ) {
        if (record == null || !record.hasStoredResponse()) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.convertValue(
                record.getResponsePayload(),
                ShipmentDetailResponse.class
        ));
    }

    @Transactional
    public void storeShipmentResponse(
            ShipmentIdempotencyRecord record,
            Shipment shipment,
            ShipmentDetailResponse response
    ) {
        record.attachShipment(shipment.getOrderId(), shipment.getId());
        record.storeResponse(
                201,
                objectMapper.convertValue(
                        response,
                        new TypeReference<Map<String, Object>>() {
                        }
                )
        );

        idempotencyRepository.save(record);
    }
}