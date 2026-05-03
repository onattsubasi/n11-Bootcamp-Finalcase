package com.onatsubasi.finalcase.auth.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to change the authenticated user's password")
public record ChangePasswordRequest(

        @NotBlank
        @Size(max = 72)
        @Schema(
                description = "Current password",
                example = "OldPassword123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String currentPassword,

        @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain at least one letter and one number"
        )
        @Schema(
                description = "New password with at least one letter and one number",
                example = "NewPassword123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String newPassword
) {
}
