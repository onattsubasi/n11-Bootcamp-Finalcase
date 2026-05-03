package com.onatsubasi.finalcase.gateway.presentation.controller;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @RequestMapping("/{serviceName}")
    public ResponseEntity<ErrorResponse> fallback(
            @PathVariable String serviceName,
            @RequestHeader(name = PlatformHeaders.X_CORRELATION_ID, required = false)
            String correlationId
    ) {
        log.warn(
                "Gateway fallback triggered, serviceName={}, correlationId={}",
                serviceName,
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(
                        false,
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "GATEWAY_DOWNSTREAM_UNAVAILABLE",
                        serviceName + " is temporarily unavailable",
                        correlationId,
                        Instant.now(),
                        null
                ));
    }
}