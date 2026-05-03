package com.onatsubasi.finalcase.payment.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestHashService {

    private final ObjectMapper objectMapper;

    public String hash(Object request) {
        try {
            String json = objectMapper.writeValueAsString(request);

            byte[] encoded = MessageDigest
                    .getInstance("SHA-256")
                    .digest(json.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(encoded);
        } catch (Exception ex) {
            log.error("event=payment.request_hash_failed", ex);

            throw new BaseException(
                    PaymentErrorCode.INVALID_PAYMENT_DATA,
                    "Failed to calculate payment request hash");
        }
    }
}