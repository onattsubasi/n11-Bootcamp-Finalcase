package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateUserPreferenceRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserPreferenceResponse;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import com.onatsubasi.finalcase.user.domain.entity.UserPreference;
import com.onatsubasi.finalcase.user.domain.repository.UserPreferenceRepository;
import com.onatsubasi.finalcase.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserPreferenceResponse getMyPreferences(UserContext userContext) {
        UUID userId = requireUserId(userContext);

        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    MDC.put("eventName", "user.preference.created");
                    MDC.put("userId", userId.toString());

                    UserPreference created = preferenceRepository.save(UserPreference.createDefault(userId));
                    log.info("User preferences lazily created, userId={}", userId);

                    MDC.remove("eventName");
                    MDC.remove("userId");
                    return created;
                });

        return userMapper.toResponse(preference);
    }

    @Transactional
    public UserPreferenceResponse updateMyPreferences(
            UserContext userContext,
            UpdateUserPreferenceRequest request
    ) {
        UUID userId = requireUserId(userContext);

        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> preferenceRepository.save(UserPreference.createDefault(userId)));

        preference.update(
                request.language(),
                request.currency(),
                request.marketingEmailEnabled(),
                request.notificationEmailEnabled(),
                request.notificationInAppEnabled()
        );

        UserPreference saved = preferenceRepository.save(preference);

        MDC.put("eventName", "user.preference.updated");
        MDC.put("userId", userId.toString());
        log.info("User preferences updated, userId={}", userId);
        MDC.remove("eventName");
        MDC.remove("userId");

        return userMapper.toResponse(saved);
    }

    private UUID requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new BaseException(UserErrorCode.INVALID_USER_ID);
        }

        return userContext.userId();
    }
}
