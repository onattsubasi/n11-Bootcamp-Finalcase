package com.onatsubasi.finalcase.user.domain.repository;

import com.onatsubasi.finalcase.user.domain.entity.UserPreference;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferenceRepository {

    UserPreference save(UserPreference preference);

    Optional<UserPreference> findByUserId(UUID userId);
}
