package com.onatsubasi.finalcase.auth.domain.entity;

import com.onatsubasi.finalcase.auth.domain.enums.AuthAccountStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "auth_users",
        indexes = {
                @Index(name = "idx_auth_users_email", columnList = "email", unique = true),
                @Index(name = "idx_auth_users_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "auth_user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role", nullable = false, length = 50)
    private Set<String> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuthAccountStatus status = AuthAccountStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AuthUser(String email, String passwordHash, Set<String> roles) {
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.roles = normalizeRoles(roles);
        this.status = AuthAccountStatus.ACTIVE;
    }

    @PrePersist
    @PreUpdate
    void normalize() {
        this.email = normalizeEmail(this.email);
        this.roles = normalizeRoles(this.roles);
        if (this.status == null) {
            this.status = AuthAccountStatus.ACTIVE;
        }
    }

    public Set<String> getRoles() {
        return Set.copyOf(roles);
    }

    public boolean isActive() {
        return status != null && status.canAuthenticate();
    }

    public void changePasswordHash(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
        this.passwordHash = encodedPassword;
    }

    public void disable() {
        this.status = AuthAccountStatus.DISABLED;
    }

    public void enable() {
        this.status = AuthAccountStatus.ACTIVE;
    }

    public void lock() {
        this.status = AuthAccountStatus.LOCKED;
    }

    public void delete() {
        this.status = AuthAccountStatus.DELETED;
    }

    public void addRole(String role) {
        String normalizedRole = normalizeRole(role);
        if (normalizedRole != null) {
            roles.add(normalizedRole);
        }
    }

    public void removeRole(String role) {
        String normalizedRole = normalizeRole(role);
        if (normalizedRole != null) {
            roles.remove(normalizedRole);
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static Set<String> normalizeRoles(Set<String> roles) {
        if (roles == null) {
            return new HashSet<>();
        }

        return roles.stream()
                .map(AuthUser::normalizeRole)
                .filter(role -> role != null && !role.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        return normalized;
    }
}
