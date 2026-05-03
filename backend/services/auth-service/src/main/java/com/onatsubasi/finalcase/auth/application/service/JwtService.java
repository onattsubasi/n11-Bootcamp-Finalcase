package com.onatsubasi.finalcase.auth.application.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMillis;
    private final String issuer;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMillis,
            @Value("${jwt.issuer:finalcase-auth-service}") String issuer
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured");
        }

        if (accessTokenExpirationMillis <= 0) {
            throw new IllegalStateException("JWT access token expiration must be greater than zero");
        }

        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (Exception ex) {
            throw new IllegalStateException("JWT_SECRET must be Base64-encoded", ex);
        }

        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.issuer = issuer;
    }

    public String generateAccessToken(UUID userId, String email, Set<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(accessTokenExpirationMillis);

        List<String> roleClaims = roles == null
                ? List.of()
                : roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(this::normalizeRole)
                .sorted()
                .toList();

        logTokenIssued(userId, expiresAt);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("roles", roleClaims)
                .signWith(signingKey)
                .compact();
    }

    public long accessTokenExpiresInSeconds() {
        return accessTokenExpirationMillis / 1000;
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            return normalized.substring("ROLE_".length());
        }
        return normalized;
    }

    private void logTokenIssued(UUID userId, Instant expiresAt) {
        String previousEventName = MDC.get("eventName");

        try {
            MDC.put("eventName", "auth.access_token.issued");
            log.debug("Access token issued for userId={}, expiresAt={}", userId, expiresAt);
        } finally {
            if (previousEventName == null) {
                MDC.remove("eventName");
            } else {
                MDC.put("eventName", previousEventName);
            }
        }
    }
}
