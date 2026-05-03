package com.onatsubasi.finalcase.gateway.infrastructure.filter;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = resolveCorrelationId(
                exchange.getRequest().getHeaders().getFirst(PlatformHeaders.X_CORRELATION_ID)
        );

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> headers.set(PlatformHeaders.X_CORRELATION_ID, correlationId))
                .build();

        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().set(PlatformHeaders.X_CORRELATION_ID, correlationId);
            return Mono.empty();
        });

        MDC.put("correlationId", correlationId);

        return chain.filter(exchange.mutate().request(request).build())
                .doFinally(signalType -> MDC.remove("correlationId"));
    }

    private String resolveCorrelationId(String raw) {
        if (raw == null || raw.isBlank() || raw.length() > 100) {
            return UUID.randomUUID().toString();
        }

        return raw.trim();
    }

    @Override
    public int getOrder() {
        return -200;
    }
}