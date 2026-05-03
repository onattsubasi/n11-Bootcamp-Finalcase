package com.onatsubasi.finalcase.shipment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import com.onatsubasi.finalcase.shipment.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShipmentNumberGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .withZone(ZoneOffset.UTC);

    private final ShipmentRepository shipmentRepository;

    public String generate() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = "SHP-" +
                    FORMATTER.format(Instant.now()) +
                    "-" +
                    randomSuffix();

            if (!shipmentRepository.existsByShipmentNumber(candidate)) {
                return candidate;
            }
        }

        throw new BaseException(ShipmentErrorCode.SHIPMENT_NUMBER_GENERATION_FAILED);
    }

    private String randomSuffix() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }
}
