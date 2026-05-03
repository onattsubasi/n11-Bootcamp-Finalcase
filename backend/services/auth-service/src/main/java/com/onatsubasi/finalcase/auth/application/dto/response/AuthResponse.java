package com.onatsubasi.finalcase.auth.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Authentication response containing access token and authenticated account summary")
public record AuthResponse(

        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Access token type", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token lifetime in seconds", example = "900")
        long expiresInSeconds,

        @Schema(description = "Platform-wide user/account id")
        UUID userId,

        @Schema(description = "Authenticated account email", example = "customer@example.com")
        String email,

        @Schema(description = "Assigned roles", example = "[\"CUSTOMER\"]")
        Set<String> roles,

        @Schema(description = "Access token issue time")
        Instant issuedAt
) {
    public AuthResponse {
        roles = roles == null ? Collections.emptySet() : Set.copyOf(roles);
        issuedAt = issuedAt == null ? Instant.now() : issuedAt;
    }
}