package com.onatsubasi.finalcase.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update user preferences")
public record UpdateUserPreferenceRequest(

        @Size(max = 10)
        String language,

        @Size(min = 3, max = 3)
        String currency,

        boolean marketingEmailEnabled,

        boolean notificationEmailEnabled,

        boolean notificationInAppEnabled
) {
}
