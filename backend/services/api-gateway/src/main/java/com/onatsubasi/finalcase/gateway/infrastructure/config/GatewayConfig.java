package com.onatsubasi.finalcase.gateway.infrastructure.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class GatewayConfig {

    public static final String AUTHENTICATED_USER_ID_ATTRIBUTE = "authenticatedUserId";

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            Object authenticatedUserId = exchange.getAttribute(AUTHENTICATED_USER_ID_ATTRIBUTE);

            if (authenticatedUserId != null) {
                String userId = authenticatedUserId.toString();

                if (!userId.isBlank()) {
                    return Mono.just("user:" + userId);
                }
            }

            String forwardedFor = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For");

            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return Mono.just("ip:" + forwardedFor.split(",")[0].trim());
            }

            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();

            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                return Mono.just("ip:" + remoteAddress.getAddress().getHostAddress());
            }

            return Mono.just("anonymous:unknown");
        };
    }
}