package com.onatsubasi.finalcase.user.application.dto.response;

import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String avatarUrl,
        String language,
        boolean marketingOptIn,
        UserProfileStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}