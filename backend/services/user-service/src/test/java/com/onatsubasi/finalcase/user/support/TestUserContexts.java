package com.onatsubasi.finalcase.user.support;

import com.onatsubasi.finalcase.common.security.UserContext;

import java.util.Set;
import java.util.UUID;

public final class TestUserContexts {

    private TestUserContexts() {
    }

    public static UserContext customer(UUID userId, String email) {
        return new UserContext(userId, email, Set.of("CUSTOMER"));
    }

    public static UserContext admin(UUID userId, String email) {
        return new UserContext(userId, email, Set.of("ADMIN"));
    }

    public static UserContext anonymous() {
        return new UserContext(null, null, Set.of());
    }
}
