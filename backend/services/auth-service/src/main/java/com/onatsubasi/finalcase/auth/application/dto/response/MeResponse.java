package com.onatsubasi.finalcase.auth.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Authenticated account summary")
public record MeResponse(
        @Schema(description = "Platform-wide user/account id")
        UUID userId,

        @Schema(description = "Account email", example = "customer@example.com")
        String email,

        @Schema(description = "Account roles", example = "[\"CUSTOMER\"]")
        Set<String> roles
) {
    public MeResponse {
        roles = roles == null ? Collections.emptySet() : Set.copyOf(roles);
    }
}
