package com.onatsubasi.finalcase.auth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.refresh-token")
public class AuthRefreshTokenProperties {

    /**
     * Long random secret used for HMAC hashing refresh tokens.
     */
    private String pepper;

    /**
     * Refresh token expiration in seconds.
     */
    private long expirationSeconds = 604800;

    /**
     * Refresh token cookie name.
     */
    private String cookieName = "refresh_token";

    /**
     * Refresh token cookie path.
     */
    private String cookiePath = "/api/auth";

    /**
     * Whether refresh cookie should be Secure.
     */
    private boolean cookieSecure = false;

    /**
     * Refresh cookie SameSite policy.
     */
    private String cookieSameSite = "Strict";
}