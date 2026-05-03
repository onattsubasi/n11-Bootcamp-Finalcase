package com.onatsubasi.finalcase.auth.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration request. Auth Service owns credentials only; profile fields are managed by User Service.")
public record RegisterRequest(

        @NotBlank
        @Email
        @Size(max = 320)
        @Schema(
                description = "Account email address",
                example = "customer@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain at least one letter and one number"
        )
        @Schema(
                description = "Password with at least one letter and one number",
                example = "Password123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String password
) {
}
