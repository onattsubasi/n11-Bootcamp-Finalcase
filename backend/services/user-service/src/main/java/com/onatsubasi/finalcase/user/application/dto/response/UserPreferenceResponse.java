package com.onatsubasi.finalcase.user.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserPreferenceResponse(
        UUID id,
        UUID userId,
        String language,
        String currency,
        boolean marketingEmailEnabled,
        boolean notificationEmailEnabled,
        boolean notificationInAppEnabled,
        Instant createdAt,
        Instant updatedAt
) {
}
