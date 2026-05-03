package com.onatsubasi.finalcase.common.security;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserContext(
        UUID userId,
        String email,
        Set<String> roles
) {

    public UserContext {
        email = normalize(email);
        roles = roles == null
                ? Collections.emptySet()
                : roles.stream()
                  .filter(role -> role != null && !role.isBlank())
                  .map(UserContext::normalizeRole)
                  .filter(role -> !role.isBlank())
                  .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isAuthenticated() {
        return userId != null;
    }

    public boolean hasRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }

        String normalizedRole = normalizeRole(role);

        return roles.stream()
                .anyMatch(existingRole -> existingRole.equals(normalizedRole));
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isSeller() {
        return hasRole("SELLER");
    }

    public boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    public static UserContext anonymous() {
        return new UserContext(null, null, Collections.emptySet());
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            return normalized.substring("ROLE_".length());
        }

        return normalized;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}