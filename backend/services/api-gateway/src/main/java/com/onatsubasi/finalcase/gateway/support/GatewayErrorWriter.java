package com.onatsubasi.finalcase.gateway.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.ErrorCode;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class GatewayErrorWriter {

    private final ObjectMapper objectMapper;

    public GatewayErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(
            ServerWebExchange exchange,
            ErrorCode errorCode
    ) {
        return write(
                exchange,
                HttpStatus.valueOf(errorCode.httpStatus()),
                errorCode.code(),
                errorCode.defaultMessage()
        );
    }

    public Mono<Void> write(
            ServerWebExchange exchange,
            ErrorCode errorCode,
            String message
    ) {
        return write(
                exchange,
                HttpStatus.valueOf(errorCode.httpStatus()),
                errorCode.code(),
                message
        );
    }

    public Mono<Void> write(
            ServerWebExchange exchange,
            HttpStatus status,
            String errorCode,
            String message
    ) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.empty();
        }

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(PlatformHeaders.X_CORRELATION_ID);

        ErrorResponse body = new ErrorResponse(
                false,
                status.value(),
                errorCode,
                message,
                correlationId,
                Instant.now(),
                null
        );

        byte[] bytes;

        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException ex) {
            bytes = "{}".getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse()
                .writeWith(Mono.just(
                        exchange.getResponse()
                                .bufferFactory()
                                .wrap(bytes)
                ));
    }
}