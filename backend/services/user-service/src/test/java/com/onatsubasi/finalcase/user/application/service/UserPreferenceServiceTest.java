package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateUserPreferenceRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserPreferenceResponse;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import com.onatsubasi.finalcase.user.domain.entity.UserPreference;
import com.onatsubasi.finalcase.user.domain.repository.UserPreferenceRepository;
import com.onatsubasi.finalcase.user.infrastructure.mapper.UserMapper;
import com.onatsubasi.finalcase.user.support.TestUserContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private UserPreferenceRepository preferenceRepository;

    private UserPreferenceService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new UserPreferenceService(preferenceRepository, new UserMapper());
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("preferences are lazily created with practical defaults")
    void getPreferencesCreatesDefaultWhenMissing() {
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(UserPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferenceResponse response = service.getMyPreferences(TestUserContexts.customer(userId, "user@example.com"));

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.language()).isEqualTo("tr");
        assertThat(response.currency()).isEqualTo("TRY");
        assertThat(response.notificationEmailEnabled()).isTrue();
        assertThat(response.notificationInAppEnabled()).isTrue();
    }

    @Test
    @DisplayName("preference update normalizes language and currency")
    void updatePreferencesNormalizesValues() {
        UserPreference preference = UserPreference.createDefault(userId);
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));
        when(preferenceRepository.save(any(UserPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferenceResponse response = service.updateMyPreferences(
                TestUserContexts.customer(userId, "user@example.com"),
                new UpdateUserPreferenceRequest("EN", "usd", true, false, true)
        );

        assertThat(response.language()).isEqualTo("en");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.marketingEmailEnabled()).isTrue();
        assertThat(response.notificationEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("invalid currency is rejected by domain rule")
    void invalidCurrencyRejected() {
        UserPreference preference = UserPreference.createDefault(userId);
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));

        assertThatThrownBy(() -> service.updateMyPreferences(
                TestUserContexts.customer(userId, "user@example.com"),
                new UpdateUserPreferenceRequest("tr", "TRYX", true, true, true)
        ))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_PREFERENCE_DATA);
    }
}
