package com.onatsubasi.finalcase.gateway.infrastructure.config;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(
            @Value("${gateway.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
            String allowedOrigins
    ) {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();

        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));
        config.setAllowedHeaders(List.of(
                PlatformHeaders.AUTHORIZATION,
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                PlatformHeaders.X_CORRELATION_ID,
                "Idempotency-Key"
        ));
        config.setExposedHeaders(List.of(
                PlatformHeaders.X_CORRELATION_ID
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}