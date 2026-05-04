package com.onatsubasi.finalcase.user.domain.repository;

import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {

    UserProfile save(UserProfile userProfile);

    Optional<UserProfile> findByUserId(UUID userId);

    Optional<UserProfile> findByUserIdForUpdate(UUID userId);

    boolean existsByUserId(UUID userId);

    List<UserProfile> findByStatus(UserProfileStatus status);
}