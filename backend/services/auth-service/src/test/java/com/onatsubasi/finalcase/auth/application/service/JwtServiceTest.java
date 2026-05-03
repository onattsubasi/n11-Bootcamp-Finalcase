package com.onatsubasi.finalcase.auth.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("super-secret-key-that-is-at-least-32-bytes-long-for-hmac-sha".getBytes());
    private static final long EXPIRATION = 3_600_000;
    private static final String ISSUER = "test-issuer";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION, ISSUER);
    }

    @Test
    @DisplayName("Should generate a valid access token")
    void shouldGenerateValidAccessToken() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Set<String> roles = Set.of("CUSTOMER", "ROLE_ADMIN");

        String token = jwtService.generateAccessToken(userId, email, roles);

        assertThat(token).isNotBlank();

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.get("userId")).isEqualTo(userId.toString());
        assertThat(claims.get("email")).isEqualTo(email);
        assertThat(claims.get("roles")).asList().containsExactlyInAnyOrder("ADMIN", "CUSTOMER");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("Should return correct expiration in seconds")
    void shouldReturnCorrectExpirationInSeconds() {
        assertThat(jwtService.accessTokenExpiresInSeconds()).isEqualTo(EXPIRATION / 1000);
    }

    @ParameterizedTest
    @CsvSource({
            ", 3600000, JWT_SECRET must be configured",
            "'', 3600000, JWT_SECRET must be configured",
            "secret, 0, JWT access token expiration must be greater than zero",
            "secret, -1, JWT access token expiration must be greater than zero"
    })
    @DisplayName("Should throw exception for invalid configuration")
    void shouldThrowExceptionForInvalidConfig(String secret, long expiration, String expectedMessage) {
        assertThatThrownBy(() -> new JwtService(secret, expiration, ISSUER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    @DisplayName("Should throw exception for non-base64 secret")
    void shouldThrowExceptionForNonBase64Secret() {
        assertThatThrownBy(() -> new JwtService("invalid-base64!", EXPIRATION, ISSUER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET must be Base64-encoded");
    }
}
