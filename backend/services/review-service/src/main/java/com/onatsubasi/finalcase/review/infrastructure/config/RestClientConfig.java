package com.onatsubasi.finalcase.review.infrastructure.config;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import org.slf4j.MDC;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add((request, body, execution) -> {
            String correlationId = MDC.get("correlationId");
            if (correlationId != null && !correlationId.isBlank()) {
                request.getHeaders().set(PlatformHeaders.X_CORRELATION_ID, correlationId);
            }

            request.getHeaders().set(PlatformHeaders.X_INTERNAL_REQUEST, "true");
            request.getHeaders().set(PlatformHeaders.X_SERVICE_NAME, "review-service");

            return execution.execute(request, body);
        });
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }
}
