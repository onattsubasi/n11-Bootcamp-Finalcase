package com.onatsubasi.finalcase.gateway.infrastructure.filter;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.gateway.infrastructure.config.GatewayConfig;
import com.onatsubasi.finalcase.gateway.infrastructure.security.JwtClaimsParser;
import com.onatsubasi.finalcase.gateway.support.GatewayErrorWriter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/products",
            "/api/search",
            "/api/payments/iyzico/callback",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    );

    private final JwtClaimsParser jwtClaimsParser;
    private final GatewayErrorWriter errorWriter;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        String correlationId = resolveCorrelationId(exchange);

        MDC.put("correlationId", correlationId);
        MDC.put("eventName", "gateway.request.received");

        ServerHttpRequest sanitizedRequest = removeClientProvidedIdentityHeaders(
                exchange.getRequest(),
                correlationId
        );

        ServerWebExchange sanitizedExchange = exchange.mutate()
                .request(sanitizedRequest)
                .build();

        sanitizedExchange.getResponse()
                .getHeaders()
                .set(PlatformHeaders.X_CORRELATION_ID, correlationId);

        if (isInternalPath(path)) {
            log.warn(
                    "Gateway blocked internal route access, method={}, path={}, correlationId={}",
                    method,
                    path,
                    correlationId
            );

            return errorWriter.write(
                    sanitizedExchange,
                    HttpStatus.NOT_FOUND,
                    "GATEWAY_ROUTE_NOT_FOUND",
                    "Resource not found"
            ).doFinally(signalType -> clearMdc());
        }

        if (isPublicPath(path)) {
            log.debug(
                    "Gateway public route allowed, method={}, path={}, correlationId={}",
                    method,
                    path,
                    correlationId
            );

            return chain.filter(sanitizedExchange)
                    .doFinally(signalType -> clearMdc());
        }

        String authHeader = sanitizedRequest.getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(PlatformHeaders.BEARER_PREFIX)) {
            log.warn(
                    "Gateway rejected missing bearer token, method={}, path={}, correlationId={}",
                    method,
                    path,
                    correlationId
            );

            return errorWriter.write(
                    sanitizedExchange,
                    HttpStatus.UNAUTHORIZED,
                    "COMMON-003",
                    "Authentication is required"
            ).doFinally(signalType -> clearMdc());
        }

        String token = authHeader.substring(PlatformHeaders.BEARER_PREFIX.length());

        try {
            Claims claims = jwtClaimsParser.parse(token);

            String subject = claims.getSubject();
            String userId = claims.get("userId", String.class);
            String email = claims.get("email", String.class);
            String roles = extractRoles(claims.get("roles"));

            if (!isValidUserClaims(subject, userId, email)) {
                log.warn(
                        "Gateway rejected token with invalid claims, method={}, path={}, correlationId={}",
                        method,
                        path,
                        correlationId
                );

                return errorWriter.write(
                        sanitizedExchange,
                        HttpStatus.UNAUTHORIZED,
                        "COMMON-003",
                        "Invalid token claims"
                ).doFinally(signalType -> clearMdc());
            }

            if (!isAuthorized(path, roles)) {
                log.warn(
                        "Gateway rejected insufficient role, method={}, path={}, userId={}, roles={}, correlationId={}",
                        method,
                        path,
                        userId,
                        roles,
                        correlationId
                );

                return errorWriter.write(
                        sanitizedExchange,
                        HttpStatus.FORBIDDEN,
                        "COMMON-004",
                        "Access denied"
                ).doFinally(signalType -> clearMdc());
            }

            MDC.put("userId", userId);
            sanitizedExchange.getAttributes()
                    .put(GatewayConfig.AUTHENTICATED_USER_ID_ATTRIBUTE, userId);

            ServerHttpRequest authenticatedRequest = sanitizedRequest.mutate()
                    .headers(headers -> {
                        headers.set(PlatformHeaders.X_USER_ID, userId);
                        headers.set(PlatformHeaders.X_USER_EMAIL, email);
                        headers.set(PlatformHeaders.X_USER_ROLES, roles);
                        headers.set(PlatformHeaders.X_CORRELATION_ID, correlationId);
                    })
                    .build();

            ServerWebExchange authenticatedExchange = sanitizedExchange.mutate()
                    .request(authenticatedRequest)
                    .build();

            authenticatedExchange.getAttributes()
                    .put(GatewayConfig.AUTHENTICATED_USER_ID_ATTRIBUTE, userId);

            log.debug(
                    "Gateway authenticated request, method={}, path={}, userId={}, roles={}, correlationId={}",
                    method,
                    path,
                    userId,
                    roles,
                    correlationId
            );

            return chain.filter(authenticatedExchange)
                    .doFinally(signalType -> clearMdc());

        } catch (JwtException | IllegalArgumentException ex) {
            log.warn(
                    "Gateway rejected invalid token, method={}, path={}, correlationId={}",
                    method,
                    path,
                    correlationId
            );

            return errorWriter.write(
                    sanitizedExchange,
                    HttpStatus.UNAUTHORIZED,
                    "COMMON-003",
                    "Invalid or expired token"
            ).doFinally(signalType -> clearMdc());
        }
    }

    private ServerHttpRequest removeClientProvidedIdentityHeaders(
            ServerHttpRequest request,
            String correlationId
    ) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove(PlatformHeaders.X_USER_ID);
                    headers.remove(PlatformHeaders.X_USER_EMAIL);
                    headers.remove(PlatformHeaders.X_USER_ROLES);

                    headers.remove("X-User-Name");
                    headers.remove("X-Auth-User");

                    headers.set(PlatformHeaders.X_CORRELATION_ID, correlationId);
                })
                .build();
    }

    private String resolveCorrelationId(ServerWebExchange exchange) {
        String existing = exchange.getRequest()
                .getHeaders()
                .getFirst(PlatformHeaders.X_CORRELATION_ID);

        if (existing != null && !existing.isBlank() && existing.length() <= 100) {
            return existing.trim();
        }

        return UUID.randomUUID().toString();
    }

    private boolean isValidUserClaims(
            String subject,
            String userId,
            String email
    ) {
        if (subject == null || subject.isBlank()) {
            return false;
        }

        if (userId == null || userId.isBlank()) {
            return false;
        }

        if (email == null || email.isBlank()) {
            return false;
        }

        try {
            UUID.fromString(userId);
            UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        return subject.equals(userId);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATH_PREFIXES.stream()
                .anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
    }

    private boolean isInternalPath(String path) {
        return path.startsWith("/internal/");
    }

    private boolean isAuthorized(
            String path,
            String roles
    ) {
        if (path.startsWith("/api/admin/")) {
            return hasRole(roles, "ADMIN");
        }

        if (path.startsWith("/api/customer/")) {
            return hasRole(roles, "CUSTOMER") || hasRole(roles, "ADMIN");
        }

        return true;
    }

    private boolean hasRole(
            String roles,
            String requiredRole
    ) {
        if (roles == null || roles.isBlank()) {
            return false;
        }

        String normalizedRequiredRole = normalizeRole(requiredRole);

        return java.util.stream.Stream.of(roles.split(","))
                .map(this::normalizeRole)
                .anyMatch(role -> role.equals(normalizedRequiredRole));
    }

    private String extractRoles(Object rawRoles) {
        if (rawRoles instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(this::normalizeRole)
                    .filter(role -> !role.isBlank())
                    .collect(Collectors.joining(","));
        }

        if (rawRoles instanceof String roles) {
            return java.util.stream.Stream.of(roles.split(","))
                    .map(this::normalizeRole)
                    .filter(role -> !role.isBlank())
                    .collect(Collectors.joining(","));
        }

        return "";
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            return normalized.substring("ROLE_".length());
        }

        return normalized;
    }

    private void clearMdc() {
        MDC.remove("correlationId");
        MDC.remove("eventName");
        MDC.remove("userId");
    }

    @Override
    public int getOrder() {
        return -100;
    }
}