package com.onatsubasi.finalcase.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update current user's profile")
public record UpdateUserProfileRequest(

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 30)
        String phoneNumber,

        @Size(max = 1000)
        String avatarUrl,

        @Size(max = 10)
        String language,

        boolean marketingOptIn
) {
}
