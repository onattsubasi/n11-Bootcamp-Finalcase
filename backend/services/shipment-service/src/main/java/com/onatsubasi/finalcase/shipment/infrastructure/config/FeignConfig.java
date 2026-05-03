package com.onatsubasi.finalcase.shipment.infrastructure.config;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor correlationIdRequestInterceptor() {
        return template -> {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

            if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
                return;
            }

            HttpServletRequest request = servletRequestAttributes.getRequest();

            copyHeader(template, request, PlatformHeaders.X_CORRELATION_ID);
            copyHeader(template, request, PlatformHeaders.X_USER_ID);
            copyHeader(template, request, PlatformHeaders.X_USER_EMAIL);
            copyHeader(template, request, PlatformHeaders.X_USER_ROLES);
        };
    }

    private void copyHeader(
            feign.RequestTemplate template,
            HttpServletRequest request,
            String headerName
    ) {
        String value = request.getHeader(headerName);

        if (value != null && !value.isBlank()) {
            template.header(headerName, value);
        }
    }
}