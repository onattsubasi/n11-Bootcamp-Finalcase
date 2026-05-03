package com.onatsubasi.finalcase.checkout.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class CheckoutRequestHashService {

    private final ObjectMapper objectMapper;

    public CheckoutRequestHashService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String hash(Object request) {
        try {
            String json = objectMapper.writeValueAsString(request);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encoded = digest.digest(
                    json.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(encoded);
        } catch (Exception ex) {
            throw new BaseException(
                    CheckoutErrorCode.INVALID_CHECKOUT_DATA,
                    "Failed to calculate checkout request hash"
            );
        }
    }
}