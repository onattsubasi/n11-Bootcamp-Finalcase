package com.onatsubasi.finalcase.user.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;

import java.time.Instant;
import java.util.UUID;

public record UserProfilePayload(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String language,
        boolean marketingOptIn,
        UserProfileStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserProfilePayload from(UserProfile profile) {
        return new UserProfilePayload(
                profile.getUserId(),
                profile.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getLanguage(),
                profile.isMarketingOptIn(),
                profile.getStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
