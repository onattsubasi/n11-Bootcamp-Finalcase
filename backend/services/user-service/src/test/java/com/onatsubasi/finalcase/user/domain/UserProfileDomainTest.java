package com.onatsubasi.finalcase.user.domain;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileDomainTest {

    @Test
    @DisplayName("createLazy normalizes Auth-owned email reference and default language")
    void createLazyNormalizesEmailAndLanguage() {
        UUID userId = UUID.randomUUID();

        UserProfile profile = UserProfile.createLazy(userId, " OYTUN@Example.COM ", null);

        assertThat(profile.getUserId()).isEqualTo(userId);
        assertThat(profile.getEmail()).isEqualTo("oytun@example.com");
        assertThat(profile.getLanguage()).isEqualTo("tr");
        assertThat(profile.getStatus()).isEqualTo(UserProfileStatus.ACTIVE);
    }

    @Test
    @DisplayName("refreshEmailReference returns false when copied Auth email did not change")
    void refreshEmailReferenceReturnsFalseWhenUnchanged() {
        UserProfile profile = UserProfile.createLazy(UUID.randomUUID(), "user@example.com", "tr");

        boolean changed = profile.refreshEmailReference(" USER@example.com ");

        assertThat(changed).isFalse();
        assertThat(profile.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("disabled profile cannot be edited by customer profile update")
    void disabledProfileCannotBeEdited() {
        UserProfile profile = UserProfile.createLazy(UUID.randomUUID(), "user@example.com", "tr");
        profile.disable();

        assertThatThrownBy(() -> profile.updateProfile("A", "B", null, null, "tr", true))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_PROFILE_DISABLED);
    }
}
