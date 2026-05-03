package com.onatsubasi.finalcase.auth.infrastructure.security;

import com.onatsubasi.finalcase.auth.infrastructure.config.AuthRefreshTokenProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class CookieFactory {

    private final AuthRefreshTokenProperties properties;

    public CookieFactory(AuthRefreshTokenProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken, Instant expiresAt) {
        Duration maxAge = Duration.between(Instant.now(), expiresAt);

        return ResponseCookie.from(properties.getCookieName(), refreshToken)
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .sameSite(properties.getCookieSameSite())
                .path(properties.getCookiePath())
                .maxAge(maxAge.isNegative() ? Duration.ZERO : maxAge)
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(properties.getCookieName(), "")
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .sameSite(properties.getCookieSameSite())
                .path(properties.getCookiePath())
                .maxAge(Duration.ZERO)
                .build();
    }
}