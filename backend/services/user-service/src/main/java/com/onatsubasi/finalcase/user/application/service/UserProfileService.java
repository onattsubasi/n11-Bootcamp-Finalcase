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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;

    @Transactional
    public UserProfileResponse getOrCreateMyProfile(UserContext userContext) {
        UserProfile profile = getOrCreateProfileEntity(userContext);
        return userMapper.toResponse(profile);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(
            UserContext userContext,
            UpdateUserProfileRequest request
    ) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "user.profile.update.started");
            MDC.put("userId", userId.toString());

            UserProfile profile = getOrCreateProfileEntity(userContext);

            profile.updateProfile(
                    request.firstName(),
                    request.lastName(),
                    request.phoneNumber(),
                    request.avatarUrl(),
                    request.language(),
                    request.marketingOptIn()
            );

            UserProfile saved = profileRepository.save(profile);
            eventPublisher.publishProfileUpdated(saved);

            MDC.put("eventName", "user.profile.updated");
            log.info("User profile updated, userId={}", saved.getUserId());

            return userMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("user.profile.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getByUserId(UUID userId) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_PROFILE_NOT_FOUND));

        return userMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> listByStatus(UserProfileStatus status) {
        UserProfileStatus effectiveStatus = status == null ? UserProfileStatus.ACTIVE : status;

        return profileRepository.findByStatus(effectiveStatus)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserProfileResponse disableProfile(UUID userId) {
        UserProfile profile = profileRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_PROFILE_NOT_FOUND));

        profile.disable();

        UserProfile saved = profileRepository.save(profile);
        eventPublisher.publishProfileUpdated(saved);

        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserProfileResponse activateProfile(UUID userId) {
        UserProfile profile = profileRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_PROFILE_NOT_FOUND));

        profile.activate();

        UserProfile saved = profileRepository.save(profile);
        eventPublisher.publishProfileUpdated(saved);

        return userMapper.toResponse(saved);
    }

    @Transactional
    public void deleteProfile(UUID userId) {
        UserProfile profile = profileRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_PROFILE_NOT_FOUND));

        profile.softDelete();

        UserProfile saved = profileRepository.save(profile);
        eventPublisher.publishProfileUpdated(saved);
    }

    public UserProfile getOrCreateProfileEntity(UserContext userContext) {
        UUID userId = requireUserId(userContext);
        String email = requireEmail(userContext);

        return profileRepository.findByUserIdForUpdate(userId)
                .map(profile -> {
                    boolean emailChanged = profile.refreshEmailReference(email);
                    return emailChanged ? profileRepository.save(profile) : profile;
                })
                .orElseGet(() -> createProfile(userId, email));
    }

    private UserProfile createProfile(UUID userId, String email) {
        try {
            MDC.put("eventName", "user.profile.create.started");
            MDC.put("userId", userId.toString());

            UserProfile profile = UserProfile.createLazy(userId, email, "tr");
            UserProfile saved = profileRepository.save(profile);

            eventPublisher.publishProfileCreated(saved);

            MDC.put("eventName", "user.profile.created");
            log.info("User profile lazily created, userId={}", saved.getUserId());

            return saved;
        } catch (BaseException ex) {
            logBusinessFailure("user.profile.create.failed", ex);
            throw ex;
        }
    }

    private UUID requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new BaseException(UserErrorCode.INVALID_USER_ID);
        }

        return userContext.userId();
    }

    private String requireEmail(UserContext userContext) {
        if (userContext == null || userContext.email() == null || userContext.email().isBlank()) {
            throw new BaseException(UserErrorCode.INVALID_EMAIL);
        }

        return userContext.email();
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("User profile operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
        MDC.remove("userId");
    }
}
