package com.onatsubasi.finalcase.auth.domain.event;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Account-registration event. Profile attributes intentionally do not live in
 * Auth Service; User Service may create the profile lazily or consume this event.
 */
public record UserRegisteredIntegrationEvent(
        UUID userId,
        String email,
        Set<String> roles
) {
    public UserRegisteredIntegrationEvent {
        roles = roles == null ? Collections.emptySet() : Set.copyOf(roles);
    }
}
