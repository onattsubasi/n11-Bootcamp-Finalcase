package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.AbstractIntegrationTest;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;
import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserProfileRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SpringDataUserProfileJpaRepository repository;

    @Test
    @DisplayName("Should find profile by user id")
    void shouldFindProfileByUserId() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.createLazy(userId, "test@example.com", "tr");

        repository.saveAndFlush(profile);

        Optional<UserProfile> found = repository.findByUserId(userId);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should find profiles by status")
    void shouldFindProfilesByStatus() {
        UserProfile active = UserProfile.createLazy(UUID.randomUUID(), "active@example.com", "tr");
        UserProfile deleted = UserProfile.createLazy(UUID.randomUUID(), "deleted@example.com", "tr");
        deleted.softDelete();

        repository.save(active);
        repository.save(deleted);
        repository.flush();

        List<UserProfile> activeProfiles = repository.findByStatus(UserProfileStatus.ACTIVE);
        List<UserProfile> deletedProfiles = repository.findByStatus(UserProfileStatus.DELETED);

        assertThat(activeProfiles).anyMatch(p -> p.getEmail().equals("active@example.com"));
        assertThat(deletedProfiles).anyMatch(p -> p.getEmail().equals("deleted@example.com"));
    }
}