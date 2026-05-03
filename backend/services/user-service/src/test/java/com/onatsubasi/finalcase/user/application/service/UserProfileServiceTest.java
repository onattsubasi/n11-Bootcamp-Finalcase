package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateUserProfileRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserProfileResponse;
import com.onatsubasi.finalcase.user.application.port.UserEventPublisher;
import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;
import com.onatsubasi.finalcase.user.domain.repository.UserProfileRepository;
import com.onatsubasi.finalcase.user.infrastructure.mapper.UserMapper;
import com.onatsubasi.finalcase.user.support.TestUserContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private UserEventPublisher eventPublisher;

    private UserProfileService service;

    private UUID userId;
    private UserContext userContext;

    @BeforeEach
    void setUp() {
        service = new UserProfileService(profileRepository, new UserMapper(), eventPublisher);
        userId = UUID.randomUUID();
        userContext = TestUserContexts.customer(userId, "user@example.com");
    }

    @Test
    @DisplayName("getOrCreate lazily creates profile using AuthAccount id from Gateway context")
    void getOrCreateCreatesMissingProfile() {
        when(profileRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse result = service.getOrCreateMyProfile(userContext);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("user@example.com");
        verify(profileRepository).save(any(UserProfile.class));
        verify(eventPublisher).publishProfileCreated(any(UserProfile.class));
    }

    @Test
    @DisplayName("getOrCreate does not write when existing profile email reference is unchanged")
    void getOrCreateDoesNotSaveUnchangedProfile() {
        UserProfile profile = UserProfile.createLazy(userId, "user@example.com", "tr");
        when(profileRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(profile));

        UserProfileResponse result = service.getOrCreateMyProfile(userContext);

        assertThat(result.email()).isEqualTo("user@example.com");
        verify(profileRepository, never()).save(any(UserProfile.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("getOrCreate updates only denormalized email reference if Auth email changed")
    void getOrCreateRefreshesChangedEmailReference() {
        UserProfile profile = UserProfile.createLazy(userId, "old@example.com", "tr");
        when(profileRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse result = service.getOrCreateMyProfile(userContext);

        assertThat(result.email()).isEqualTo("user@example.com");
        verify(profileRepository).save(profile);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("update profile modifies only profile-owned fields, not login identity")
    void updateProfileChangesProfileFieldsOnly() {
        UserProfile profile = UserProfile.createLazy(userId, "user@example.com", "tr");
        when(profileRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                " Oytun ",
                " Coban ",
                "+905551112233",
                "https://cdn.example.com/a.png",
                "EN",
                true
        );

        UserProfileResponse result = service.updateMyProfile(userContext, request);

        assertThat(result.firstName()).isEqualTo("Oytun");
        assertThat(result.lastName()).isEqualTo("Coban");
        assertThat(result.language()).isEqualTo("en");
        assertThat(result.email()).isEqualTo("user@example.com");
        verify(eventPublisher).publishProfileUpdated(profile);
    }

    @Test
    @DisplayName("admin list by status delegates status filter without mixing Auth roles")
    void listByStatusUsesProfileStatusOnly() {
        UserProfile disabled = UserProfile.createLazy(UUID.randomUUID(), "disabled@example.com", "tr");
        disabled.disable();
        when(profileRepository.findByStatus(UserProfileStatus.DISABLED)).thenReturn(List.of(disabled));

        List<UserProfileResponse> result = service.listByStatus(UserProfileStatus.DISABLED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(UserProfileStatus.DISABLED);
    }

    @Test
    @DisplayName("missing user context is rejected before profile access")
    void missingUserContextIsRejected() {
        assertThatThrownBy(() -> service.getOrCreateMyProfile(TestUserContexts.anonymous()))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_USER_ID);
    }
}
