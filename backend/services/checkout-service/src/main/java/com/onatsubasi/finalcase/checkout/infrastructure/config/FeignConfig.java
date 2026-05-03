package com.onatsubasi.finalcase.checkout.infrastructure.config;

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

            String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

            if (correlationId != null && !correlationId.isBlank()) {
                template.header(PlatformHeaders.X_CORRELATION_ID, correlationId);
            }

            String userId = request.getHeader(PlatformHeaders.X_USER_ID);

            if (userId != null && !userId.isBlank()) {
                template.header(PlatformHeaders.X_USER_ID, userId);
            }

            String userEmail = request.getHeader(PlatformHeaders.X_USER_EMAIL);

            if (userEmail != null && !userEmail.isBlank()) {
                template.header(PlatformHeaders.X_USER_EMAIL, userEmail);
            }

            String roles = request.getHeader(PlatformHeaders.X_USER_ROLES);

            if (roles != null && !roles.isBlank()) {
                template.header(PlatformHeaders.X_USER_ROLES, roles);
            }
        };
    }
}