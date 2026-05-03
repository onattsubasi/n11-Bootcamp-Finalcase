package com.onatsubasi.finalcase.auth.infrastructure.persistence;

import com.onatsubasi.finalcase.auth.domain.entity.AuthUser;
import com.onatsubasi.finalcase.auth.domain.repository.AuthUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AuthUserRepositoryTest {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        AuthUser user = new AuthUser("test@example.com", "hash", Set.of("CUSTOMER"));
        authUserRepository.save(user);

        // When
        Optional<AuthUser> found = authUserRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return true if email exists")
    void shouldReturnTrueIfEmailExists() {
        // Given
        AuthUser user = new AuthUser("exists@example.com", "hash", Set.of("CUSTOMER"));
        authUserRepository.save(user);

        // When
        boolean exists = authUserRepository.existsByEmail("exists@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false if email does not exist")
    void shouldReturnFalseIfEmailDoesNotExist() {
        // When
        boolean exists = authUserRepository.existsByEmail("notfound@example.com");

        // Then
        assertThat(exists).isFalse();
    }
}
