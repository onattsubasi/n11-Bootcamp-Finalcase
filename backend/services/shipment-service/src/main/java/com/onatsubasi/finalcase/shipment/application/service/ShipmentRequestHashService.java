package com.onatsubasi.finalcase.shipment.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentRequestHashService {

    private final ObjectMapper objectMapper;

    public String hash(Object request) {
        try {
            String json = objectMapper.writeValueAsString(request);

            byte[] encoded = MessageDigest
                    .getInstance("SHA-256")
                    .digest(json.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(encoded);
        } catch (Exception ex) {
            log.error("event=shipment.request_hash_failed", ex);

            throw new BaseException(
                    ShipmentErrorCode.INVALID_SHIPMENT_DATA,
                    "Failed to calculate shipment request hash"
            );
        }
    }
}