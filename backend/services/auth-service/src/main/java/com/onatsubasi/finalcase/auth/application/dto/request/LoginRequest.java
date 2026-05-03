package com.onatsubasi.finalcase.auth.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Login request")
public record LoginRequest(

        @NotBlank
        @Email
        @Schema(
                description = "Account email address",
                example = "customer@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @NotBlank
        @Size(max = 72)
        @Schema(
                description = "Account password",
                example = "Password123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String password
) {
}